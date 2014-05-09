package ecommerce.sales.domain.reservation

import ReservationStatus._
import ecommerce.sales.domain.reservation.Reservation._
import ecommerce.sales.domain.inventory.ProductType
import java.util.Date
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain._
import ddd.support.domain.protocol.Acknowledged
import akka.actor.Props
import ddd.support.domain.event.DomainEvent
import ecommerce.sales.domain.reservation.Reservation.ProductReserved
import ecommerce.sales.domain.reservation.errors.ReservationCreationException
import ecommerce.sales.domain.inventory.ProductData
import ecommerce.sales.domain.reservation.Reservation.CreateReservation
import scala.Some
import ecommerce.sales.domain.reservation.errors.ReservationOperationException
import ecommerce.sales.domain.reservation.Reservation.CloseReservation
import ecommerce.sales.domain.reservation.Reservation.ReserveProduct
import ecommerce.sales.domain.reservation.Reservation.ReservationCreated
import ecommerce.sales.domain.reservation.Reservation.ReservationClosed
import infrastructure.actor.PassivationConfig

/**
 * Reservation is just a "wish list". System can not guarantee that user can buy desired products.</br>
 * // TODO
 * Reservation generates Offer VO, that is calculated based on current prices and current availability.
 *
 */
object Reservation {

  implicit val idResolution  = new ReservationIdResolution
  implicit val actorFactory = new ReservationActorFactory

  class ReservationActorFactory extends AggregateRootActorFactory[Reservation] {
    override def props(passivationConfig: PassivationConfig): Props = Props(new Reservation(passivationConfig))
  }

  class ReservationIdResolution extends AggregateIdResolution[Reservation] {
    override def aggregateIdResolver = {
      case cmd: Command => cmd.reservationId
    }
  }

  // Commands
  sealed trait Command { def reservationId: String }
  case class CreateReservation(reservationId: String, clientId: String) extends Command
  case class ReserveProduct(reservationId: String, productId: String, quantity: Int) extends Command
  case class CloseReservation(reservationId: String) extends Command

  // Events
  case class ReservationCreated(reservationId: String, clientId: String) extends DomainEvent
  case class ProductReserved(reservationId: String, product: ProductData, quantity: Int) extends DomainEvent
  case class ReservationClosed(reservationId: String) extends DomainEvent

}

class Reservation(override val passivationConfig: PassivationConfig) extends AggregateRoot[State] {

  override val factory: AggregateRootFactory = {
    case ReservationCreated(_, clientId) =>
      State(clientId, Opened, items = List.empty, createDate = new Date)
  }

  override def handleCommand: Receive = {
    case cmd: Command => cmd match {

      case CreateReservation(reservationId, clientId) =>
        if (initialized) {
          throw new ReservationCreationException(s"Reservation $reservationId already exists")
        } else {
          raise(ReservationCreated(reservationId, clientId))
        }

      case ReserveProduct(reservationId, productId, quantity) =>
        if (state.status eq Closed) {
          throw new ReservationOperationException(s"Reservation $reservationId is closed", reservationId)
        } else {
          // TODO fetch product detail
          // TODO fetch price for the client
          val product = ProductData(productId, "productName", ProductType.Standard, Money(10))
          raise(ProductReserved(reservationId, product, quantity)) { event =>
            // customized handling of ProductReserved
            publish(event)
            sender() ! Acknowledged
          }
        }

      case CloseReservation(reservationId) =>
        raise(ReservationClosed(reservationId))
    }
  }

}

case class State (
    clientId: String,
    status: ReservationStatus,
    items: List[ReservationItem],
    createDate: Date)
  extends AggregateState {

  override def apply = {

    case ProductReserved(_, product, quantity) =>
      val newItems = items.find(item => item.productId == product.productId) match {
        case Some(orderLine) =>
          val index = items.indexOf(orderLine)
          items.updated(index, orderLine.increaseQuantity(quantity))
        case None =>
          ReservationItem(product, quantity) :: items
      }
      copy(items = newItems)

    case ReservationClosed(_) => copy(status = Closed)
  }

}
