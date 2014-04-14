package ecommerce.sales.domain.reservation

import test.support.EventsourcedAggregateRootSpec
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import ecommerce.sales.domain.reservation.Reservation.ReserveProduct
import ddd.support.domain.error.AggregateRootNotInitializedException

class ReservationFailuresSpec extends EventsourcedAggregateRootSpec(ReservationSpec.testSystem) {

  override val aggregateRootId = "reservation1"

  def getReservationActor(name: String) = {
    getActor(Props[Reservation])(name)
  }

  "Reservation of product" must {
    "fail if Reservation does not exist" in {
      val reservationId = aggregateRootId
      val reservation = getReservationActor(reservationId)
      implicit val timeout = Timeout(5, SECONDS)

      // Use ask (?) to send a command and expect Failure(AggregateRootNotInitializedException) in the response
      expectFailure[AggregateRootNotInitializedException] {
        reservation ? ReserveProduct(reservationId, "product1", 1)
      }
    }
  }

}
