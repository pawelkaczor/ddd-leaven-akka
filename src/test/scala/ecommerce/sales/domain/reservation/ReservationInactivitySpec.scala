package ecommerce.sales.domain.reservation

import ecommerce.sales.domain.reservation.Reservation._
import scala.concurrent.duration._

import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Office._
import test.support.TestConfig._
import ddd.support.domain.protocol.Acknowledged

class ReservationInactivitySpec extends EventsourcedAggregateRootSpec[Reservation](testSystem) {

  import ReservationSpec.ReservationActorFactory

  "Reservation office" should {
    "passivate idle clerks" in {
      // given
      val reservationId = "reservation3"
      val reservationOffice = office[Reservation](inactivityTimeout = 50.milliseconds)

      // when
      reservationOffice ! CreateReservation(reservationId, "client1")

      // then
      expectReply(Acknowledged)

      // then / when
      expectLogMessageFromOffice("Passivating Actor") {
        Thread.sleep(50)
        reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      }
    }
  }

}
