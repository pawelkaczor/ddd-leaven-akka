package ecommerce.sales.domain.reservation

import akka.actor._
import akka.persistence._
import ddd.support.domain.event.DomainEvent
import ReservationStatus._
import ecommerce.sales.domain.reservation.Reservation._
import scala.Some
import ecommerce.sales.domain.reservation.Reservation.ProductReserved
import ecommerce.sales.domain.reservation.Reservation.CreateReservation
import ecommerce.sales.domain.reservation.errors.{ReservationOperationException, ReservationCreationException}
import ecommerce.sales.domain.productscatalog.{ProductData, ProductType}
import java.util.Date
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain.{AggregateState, AggregateRoot}

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

class Reservation extends AggregateRoot[State] {

  override val factory: AggregateRootFactory = {
    case ReservationCreated(_, clientId) =>
      State(clientId, Opened, items = List.empty, createDate = new Date)
  }

  override def receiveCommand: Receive = {
    case cmd: Command => cmd match {

      case CreateReservation(reservationId, clientId) =>
        if (created) {
          throw new ReservationCreationException(s"Reservation $reservationId already exists")
        } else {
          apply(ReservationCreated(reservationId, clientId))
        }

      case ReserveProduct(reservationId, productId, quantity) =>
        if (state.status eq Closed) {
          throw new ReservationOperationException(s"Reservation $reservationId is closed", reservationId)
        } else {
          // TODO fetch product detail
          // TODO fetch price for the client
          val product = ProductData(productId, "productName", ProductType.Standard, Money(10))
          apply(ProductReserved(reservationId, product, quantity)) { event =>
            // customized handling of ProductReserved
          }
        }

      case CloseReservation(reservationId) =>
        apply(ReservationClosed(reservationId))
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
