package ecommerce.sales.domain.reservation

import akka.actor.{Terminated, ActorRef, PoisonPill}
import ecommerce.sales.domain.productscatalog.{ProductData, ProductType}
import ecommerce.sales.domain.reservation.Reservation._
import ecommerce.sales.domain.reservation.Reservation.ReserveProduct
import ecommerce.sales.domain.reservation.Reservation.CreateReservation
import ecommerce.sales.domain.reservation.Reservation.ReservationCreated
import ecommerce.sales.domain.reservation.Reservation.ProductReserved
import scala.concurrent.duration._

import ecommerce.sales.sharedkernel.Money
import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Office._
import test.support.TestConfig._
import ddd.support.domain.protocol.Acknowledged

class ReservationSpec extends EventsourcedAggregateRootSpec[Reservation](testSystem)  {

  var reservationOffice: ActorRef = system.deadLetters

  before {
    reservationOffice = office[Reservation]
  }

  after {
    ensureActorTerminated(reservationOffice)
  }


  "Reservation clerk" must {
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

      val product2 = ProductData("product2", "productName", ProductType.Standard, Money(10))
      val quantity= 1
      expectEventPersisted(ProductReserved(reservationId, product2, quantity))(reservationId) {
        reservationOffice ! ReserveProduct(reservationId, "product2", quantity)
      }

      expectEventPersisted[ReservationClosed](reservationId) {
        reservationOffice ! CloseReservation(reservationId)
      }

    }
  }

  "Reservation office" must {
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

  private def ensureActorTerminated(actor: ActorRef) = {
    watch(actor)
    actor ! PoisonPill
    // wait until reservation office is terminated
    fishForMessage(1.seconds) {
      case Terminated(_) =>
        unwatch(actor)
        true
      case _ => false
    }

  }


}
