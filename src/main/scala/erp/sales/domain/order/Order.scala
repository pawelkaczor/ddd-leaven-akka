package erp.sales.domain.order

import akka.actor._
import akka.persistence._
import ddd.domain.sharedkernel.Money
import ddd.domain.{AggregateState, AggregateRoot, DomainEntity}
import erp.sales.domain.policies.rebate.Rebates.RebatePolicy
import java.sql.Timestamp
import erp.sales.domain.ProductType.ProductType
import erp.sales.domain.policies.rebate.{standardRebate, Rebates}
import ddd.domain.event.{AggregateRootCreated, DomainEvent}
import OrderStatus._
import erp.sales.domain.order.Order._
import scala.Some
import erp.sales.domain.order.Order.ProductAddedToOrder
import erp.sales.domain.order.Order.CreateOrder
import erp.sales.domain.order.errors.{OrderOperationException, OrderCreationException}

object Order {
  // Commands
  sealed trait Command { def orderId: String }
  case class CreateOrder(orderId: String, clientId: String) extends Command
  case class AddProduct(orderId: String, productId: String, quantity: Int) extends Command

  // Events
  sealed trait Event extends DomainEvent
  case class OrderCreated(override val id: String, clientId: String) extends AggregateRootCreated(id) with Event
  case class ProductAddedToOrder(
      productId: String,
      orderId: String,
      productType: ProductType,
      price: Money,
      quantity: Int)
    extends Event
  case class OrderArchived(id: String) extends Event

}

class Order extends AggregateRoot[State] with EventsourcedProcessor with ActorLogging {

  implicit def initialize(event: AggregateRootCreated) =
    event match {
      case OrderCreated(_, clientId) => Option(State(clientId, Draft, Money(0), List.empty, None))
      case _ => None
    }

  override def receiveRecover: Receive = {
    case evt: DomainEvent => apply(evt)
  }

  override def receiveCommand: Receive = {
    case cmd: Command => cmd match {
      case CreateOrder(orderId, clientId) =>
        if (created) {
          throw new OrderCreationException(s"Order $orderId already exists")
        } else {
          persist(OrderCreated(orderId, clientId)) { event =>
            apply(event)
            log.info("Order created: {}", orderId)
            sender() ! "accepted"
          }
        }
      case AddProduct(orderId, productId, quantity) =>
          if (getState.status ne Draft) {
            throw new OrderOperationException(s"Order $orderId already submitted", orderId)
          } else {
            // TODO fetch product detail
            // TODO fetch price for the client
            persist(ProductAddedToOrder(productId, orderId, productType = null, price = null, quantity)) { event =>
              apply(event)
              log.info("Product {} added to order: {}", productId, orderId)
            }
          }
    }
    case ReceiveTimeout => context stop self
  }

}

case class State (
    clientId: String,
    status: OrderStatus,
    totalCost: Money,
    items: List[OrderLine],
    submitDate: Option[Timestamp])
  extends AggregateState {

  def apply(event: DomainEvent): State = event match {
    case event @ ProductAddedToOrder(productId, _, _, price, quantity) =>
      val policy: Rebates.RebatePolicy = standardRebate(5, 10)
      val newItems = items.find(item => item.id == productId) match {
        case Some(orderLine) =>
          val index = items.indexOf(orderLine)
          items.updated(index, orderLine.increaseQuantity(quantity, policy))
        case None =>
          OrderLine(OrderProduct(event), quantity, policy) :: items
      }
      copy(items = newItems).recalculate(policy)
  }

  private def recalculate(policy: RebatePolicy): State = {
    val newTotalCost = items.foldLeft(Money(0))((accumulator, item) => accumulator + item.effectiveCost)
    copy(totalCost = newTotalCost, items = items.map(_.applyPolicy(policy)))
  }

}
