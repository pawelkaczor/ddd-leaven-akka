package ecommerce.sales.domain.reservation

import test.support.{ LocalOffice, EventsourcedAggregateRootSpec }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import ecommerce.sales.domain.reservation.Reservation.ReserveProduct
import ddd.support.domain.error.AggregateRootNotInitializedException
import ecommerce.system.infrastructure.office.Office._
import test.support.TestConfig._
import LocalOffice._

class ReservationFailuresSpec extends EventsourcedAggregateRootSpec[Reservation](testSystem) {

  import ReservationSpec._

  "Reservation of product" should {
    "fail if Reservation does not exist" in {
      // given
      val reservationId = "reservation1"
      implicit val timeout = Timeout(5, SECONDS)

      // then / when
      // Use ask (?) to send a command and expect Failure(AggregateRootNotInitializedException) in the response
      expectFailure[AggregateRootNotInitializedException] {
        office[Reservation] ? ReserveProduct(reservationId, "product1", 1)
      }
    }
  }

}
