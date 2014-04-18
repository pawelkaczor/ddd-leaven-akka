package ecommerce.sales.domain.reservation

import scala.concurrent.duration._
import ecommerce.sales.domain.reservation.Reservation.{ReserveProduct, CreateReservation}
import ddd.support.domain.protocol.Acknowledged
import ddd.support.domain.Representative._


class ReservationClusterSpec extends AbstractReservationClusterSpec {

  import ReservationClusterConfig._

  "Reservation global office" must {
    "given shared journal" in {
      setupSharedJournal()
    }

    "given cluster" in within(15.seconds) {
      joinCluster()
    }

    "start" in {
      startSharding[Reservation]
    }

    "handle commands from multiply nodes" in within(15.seconds) {
      val reservationId = "reservation1"

      runOn(node1) {
        val reservationOffice = globalOffice[Reservation]
        reservationOffice ! CreateReservation(reservationId, "client1")
        reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      }

      runOn(node2) {
        val reservationOffice = globalOffice[Reservation]
        awaitAssert {
          within(1.second) {
            reservationOffice ! ReserveProduct(reservationId, "product2", 1)
            expectMsg(Acknowledged)
          }
        }
      }
      enterBarrier("after-3")
    }

  }
}
