package ecommerce.sales.domain.reservation

import java.util.Date

import ddd.support.domain.BusinessEntity.EntityId
import ddd.support.domain._
import ddd.support.domain.event.{ DomainEvent, EventPublisher }
import ecommerce.sales.domain.product.Product
import ecommerce.sales.domain.reservation.Reservation._
import ecommerce.sales.domain.reservation.ReservationStatus._
import ecommerce.sales.domain.reservation.errors.{ ReservationCreationException, ReservationOperationException }
import ecommerce.sales.sharedkernel.{ Money, ProductType }
import infrastructure.actor.PassivationConfig

/**
 * Reservation is just a "wish list". System can not guarantee that user can buy desired products.</br>
 * // TODO
 * Reservation generates Offer VO, that is calculated based on current prices and current availability.
 *
 */
object Reservation {

  def processorId(aggregateId: EntityId) = "Reservations/" + aggregateId

  // Commands
  sealed trait Command extends command.Command {
    def reservationId: EntityId
    override def aggregateId = reservationId
  }

  case class CreateReservation(reservationId: EntityId, clientId: EntityId) extends Command
  case class ReserveProduct(reservationId: EntityId, productId: EntityId, quantity: Int) extends Command
  case class ConfirmReservation(reservationId: EntityId) extends Command
  case class CloseReservation(reservationId: EntityId) extends Command

  // Events
  case class ReservationCreated(reservationId: EntityId, clientId: EntityId) extends DomainEvent
  case class ProductReserved(reservationId: EntityId, product: Product, quantity: Int) extends DomainEvent
  case class ReservationConfirmed(reservationId: EntityId, clientId: EntityId) extends DomainEvent
  case class ReservationClosed(reservationId: EntityId) extends DomainEvent

  case class State(
    clientId: EntityId,
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

      case ReservationConfirmed(_, _) => copy(status = Confirmed)
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

    case ConfirmReservation(reservationId) =>
      if (state.status eq Closed) {
        throw new ReservationOperationException(s"Reservation $reservationId is closed", reservationId)
      } else {
        raise(ReservationConfirmed(reservationId, state.clientId))
      }

    case CloseReservation(reservationId) =>
      raise(ReservationClosed(reservationId))
  }

}