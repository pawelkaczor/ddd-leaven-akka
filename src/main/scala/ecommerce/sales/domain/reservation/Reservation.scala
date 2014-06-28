package ecommerce.sales.domain.reservation

import ReservationStatus._
import java.util.Date
import ecommerce.sales.sharedkernel.{ ProductType, Money }
import ddd.support.domain._
import ddd.support.domain.event.{ EventPublisher, DomainEvent }
import ecommerce.sales.domain.product.Product
import ecommerce.sales.domain.reservation.Reservation._
import ecommerce.sales.domain.reservation.errors.ReservationCreationException
import ddd.support.domain.SnapshotId
import scala.Some
import ecommerce.sales.domain.reservation.errors.ReservationOperationException
import infrastructure.actor.PassivationConfig

/**
 * Reservation is just a "wish list". System can not guarantee that user can buy desired products.</br>
 * // TODO
 * Reservation generates Offer VO, that is calculated based on current prices and current availability.
 *
 */
object Reservation {

  def processorId(aggregateId: String) = "Reservations/" + aggregateId

  // Commands
  sealed trait Command extends command.Command {
    def reservationId: String
    override def aggregateId = reservationId
  }

  case class CreateReservation(reservationId: String, clientId: String) extends Command
  case class ReserveProduct(reservationId: String, productId: String, quantity: Int) extends Command
  case class CloseReservation(reservationId: String) extends Command

  // Events
  case class ReservationCreated(reservationId: String, clientId: String) extends DomainEvent
  case class ProductReserved(reservationId: String, product: Product, quantity: Int) extends DomainEvent
  case class ReservationClosed(reservationId: String) extends DomainEvent

  case class State(
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

}

abstract class Reservation(override val passivationConfig: PassivationConfig) extends AggregateRoot[State] {
  this: EventPublisher =>

  override def processorId = Reservation.processorId(id)

  override val factory: AggregateRootFactory = {
    case ReservationCreated(_, clientId) =>
      State(clientId, Opened, items = List.empty, createDate = new Date)
  }

  override def handleCommand: Receive = {
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
        val product = Product(SnapshotId(productId, 0), "productName", ProductType.Standard, Some(Money(10)))
        raise(ProductReserved(reservationId, product, quantity))
      }

    case CloseReservation(reservationId) =>
      raise(ReservationClosed(reservationId))
  }

}