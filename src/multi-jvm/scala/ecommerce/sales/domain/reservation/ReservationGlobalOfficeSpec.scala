package ecommerce.sales.domain.reservation

import scala.concurrent.duration._
import ddd.support.domain.protocol.Acknowledged
import ddd.support.domain.Office._
import infrastructure.cluster.ShardResolution.ShardResolutionStrategy
import akka.contrib.pattern.ShardRegion._
import ecommerce.sales.domain.reservation.Reservation._
import akka.testkit.TestProbe
import ddd.support.domain.event.DomainEvent
import scala.reflect.ClassTag
import infrastructure.cluster.ReservationShardResolution


class ReservationGlobalOfficeSpec extends ReservationClusterSpec {

  import ReservationClusterConfig._

  "Reservation global office" must {
    "given necessary infrastructure available" in {
      setupSharedJournal()
      joinCluster()
    }
    "given work distribution enabled" in {
      startSharding[Reservation](new ReservationShardResolution {
        //take last char of reservationId as shard id
        override def shardResolutionStrategy: ShardResolutionStrategy =
          addressResolver => {
            case msg: Msg => addressResolver(msg).last.toString
          }
      })
    }

    enterBarrier("when")

    "distribute work evenly" in {
      val reservationOffice = globalOffice[Reservation]

      on(node1) {
        expectEventPublished[ReservationCreated] {
            reservationOffice ! CreateReservation("reservation-1", "client1")
            reservationOffice ! CreateReservation("reservation-2", "client2")
        }
      }

      on(node2) {
        expectEventPublished[ReservationCreated]
      }
    }

    "handle subsequent commands from anywhere" in {
      val reservationOffice = globalOffice[Reservation]

      on(node2) {
        expectReply(Acknowledged) {
          reservationOffice ! ReserveProduct("reservation-1", "product1", 1)
        }
        expectReply(Acknowledged) {
          reservationOffice ! ReserveProduct("reservation-2", "product1", 1)
        }
      }
    }

  }

  def expectReply[T, R](obj: T)(when: => R): R = {
    val r = when
    expectMsg(20.seconds, obj)
    r
  }

  def expectEventPublished[E <: DomainEvent](implicit t: ClassTag[E]) {
    expectEventPublished()
  }

  def expectEventPublished[E <: DomainEvent](when: Unit)(implicit t: ClassTag[E]) {
    val probe = TestProbe()
    system.eventStream.subscribe(probe.ref, t.runtimeClass)
    val r = when
    probe.expectMsgClass(20.seconds, t.runtimeClass)
    r
  }
}
