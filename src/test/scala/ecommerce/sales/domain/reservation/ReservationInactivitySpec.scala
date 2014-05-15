package ecommerce.sales.domain.reservation

import ecommerce.sales.domain.reservation.Reservation._
import scala.concurrent.duration._

import test.support.{LocalPublisher, EventsourcedAggregateRootSpec}
import ddd.support.domain.Office._
import test.support.TestConfig._
import ddd.support.domain.protocol.Acknowledged
import ddd.support.domain.AggregateRootActorFactory
import infrastructure.actor.PassivationConfig
import akka.actor.Props

class ReservationInactivitySpec extends EventsourcedAggregateRootSpec[Reservation](testSystem)  {

  import ReservationSpec.ReservationActorFactory

  "Reservation office" must {
    "passivate idle clerks" in {
      val reservationId = "reservation3"
      val reservationOffice = office[Reservation](inactivityTimeout = 50.milliseconds)

      expectReply(Acknowledged) {
        reservationOffice ! CreateReservation(reservationId, "client1")
      }

      expectLogMessageFromOffice("Passivating Actor") {
        Thread.sleep(50)
        reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      }
    }
  }

}
