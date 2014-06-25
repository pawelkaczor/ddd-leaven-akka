package ecommerce.sales.domain.reservation

import ecommerce.system.infrastructure.office.Office._
import ddd.support.domain.protocol.Acknowledged
import ecommerce.sales.domain.reservation.Reservation._
import test.support.EventsourcedAggregateRootSpec
import test.support.LocalOffice._
import test.support.TestConfig._

import scala.concurrent.duration._

class ReservationInactivitySpec extends EventsourcedAggregateRootSpec[Reservation](testSystem) {

  implicit def reservationActorFactory = ReservationSpec.reservationActorFactory(50.milliseconds)

  "Reservation office" should {
    "passivate idle clerks" in {
      // given
      val reservationId = "reservation3"
      val reservationOffice = office[Reservation]

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
