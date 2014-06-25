package ecommerce.sales.domain.reservation

import akka.actor.{ ActorRef, Props }
import ddd.support.domain.AggregateRootActorFactory
import ecommerce.system.infrastructure.office.Office._
import ddd.support.domain.protocol.Acknowledged
import ecommerce.sales.domain.product.Product
import ecommerce.sales.domain.reservation.Reservation._
import ecommerce.sales.sharedkernel.{ Money, ProductType }
import infrastructure.actor.PassivationConfig
import test.support.LocalOffice._
import test.support.TestConfig._
import test.support.{ EventsourcedAggregateRootSpec, LocalPublisher }
import ReservationSpec._

import scala.concurrent.duration._

object ReservationSpec {
  implicit def reservationActorFactory(implicit _inactivityTimeout: Duration = 1.minute) = new AggregateRootActorFactory[Reservation] {
    override def props(passivationConfig: PassivationConfig): Props = Props(new Reservation(passivationConfig) with LocalPublisher)

    override def inactivityTimeout: Duration = _inactivityTimeout
  }
}

class ReservationSpec extends EventsourcedAggregateRootSpec[Reservation](testSystem) {

  var reservationOffice: ActorRef = system.deadLetters

  before {
    reservationOffice = office[Reservation]
  }

  after {
    ensureActorTerminated(reservationOffice)
  }

  "Reservation clerk" should {
    "communicate outcome with events" in {
      val reservationId = "reservation1"

      expectEventPersisted[ReservationCreated](reservationId) {
        reservationOffice ! CreateReservation(reservationId, "client1")
      }
      expectEventPersisted[ProductReserved](reservationId) {
        reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      }

      // kill reservation office and all its clerks (aggregate roots)
      ensureActorTerminated(reservationOffice)
      reservationOffice = office[Reservation]

      val product2 = Product("product2", "productName", ProductType.Standard, Some(Money(10)))
      val quantity = 1
      expectEventPersisted(ProductReserved(reservationId, product2, quantity))(reservationId) {
        reservationOffice ! ReserveProduct(reservationId, "product2", quantity)
      }

      expectEventPersisted[ReservationClosed](reservationId) {
        reservationOffice ! CloseReservation(reservationId)
      }

    }
  }

  "Reservation office" should {
    "acknowledge commands" in {
      val reservationId = "reservation2"

      reservationOffice ! CreateReservation(reservationId, "client1")
      expectMsg(Acknowledged)

      reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      expectMsg(Acknowledged)

      // kill reservation office and all its clerks (aggregate roots)
      ensureActorTerminated(reservationOffice)
      reservationOffice = office[Reservation]

      reservationOffice ! ReserveProduct(reservationId, "product2", 1)
      expectMsg(Acknowledged)

      reservationOffice ! CloseReservation(reservationId)
      expectMsg(Acknowledged)

    }
  }

}
