package ecommerce.sales.domain.reservation

import akka.actor._
import akka.persistence._
import ddd.domain.sharedkernel.Money
import ddd.domain.{AggregateState, AggregateRoot}
import ddd.domain.event.DomainEvent
import ReservationStatus._
import ecommerce.sales.domain.reservation.Reservation._
import scala.Some
import ecommerce.sales.domain.reservation.Reservation.ProductReserved
import ecommerce.sales.domain.reservation.Reservation.CreateReservation
import ecommerce.sales.domain.reservation.errors.{ReservationOperationException, ReservationCreationException}
import ecommerce.sales.domain.productscatalog.{ProductData, ProductType}
import java.util.Date

/**
 * Reservation is just a "wish list". System can not guarantee that user can buy desired products.</br>
 * // TODO
 * Reservation generates Offer VO, that is calculated based on current prices and current availability.
 *
 */
object Reservation {
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

class Reservation extends AggregateRoot[State] with EventsourcedProcessor with ActorLogging {

  implicit val factory: AggregateRootFactory = {
    case ReservationCreated(_, clientId) => State(clientId, Opened, List.empty, new Date)
  }

  override def receiveRecover: Receive = {
    case evt: DomainEvent => apply(evt)
  }

  override def receiveCommand: Receive = {
    case cmd: Command => cmd match {
      case CreateReservation(reservationId, clientId) =>
        if (created) {
          throw new ReservationCreationException(s"Reservation $reservationId already exists")
        } else {
          persist(ReservationCreated(reservationId, clientId)) { event =>
            apply(event)
            log.info("Reservation created: {}", reservationId)
          }
        }
      case ReserveProduct(reservationId, productId, quantity) =>
        if (state.status eq Closed) {
          throw new ReservationOperationException(s"Reservation $reservationId is closed", reservationId)
        } else {
          // TODO fetch product detail
          // TODO fetch price for the client
          val product = ProductData(productId, "productName", ProductType.Standard, Money(10))
          persist(ProductReserved(reservationId, product, quantity)) { event =>
            apply(event)
            log.info("Product {} reserved", productId)
          }
        }
      case CloseReservation(reservationId) =>
        persist(ReservationClosed(reservationId)) { event =>
          apply(event)
          log.info("Reservation closed: {}", reservationId)
        }
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

    case event @ ProductReserved(_, product, quantity) =>
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
