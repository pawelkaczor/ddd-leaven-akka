package ecommerce.sales.domain.reservation

import java.io.File
import scala.concurrent.duration._
import org.apache.commons.io.FileUtils
import com.typesafe.config.ConfigFactory
import akka.actor.ActorIdentity
import akka.actor.Identify
import akka.actor.Props
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterSharding
import akka.persistence.Persistence
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import akka.persistence.journal.leveldb.SharedLeveldbStore
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import test.support.STMultiNodeSpec
import ecommerce.sales.domain.reservation.Reservation.{ReserveProduct, CreateReservation}
import ddd.support.domain.protocol.Acknowledged

object ReservationClusterSpec extends MultiNodeConfig {

  val controller = role("controller")
  val node1 = role("node1")
  val node2 = role("node2")

  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb-shared"
    akka.persistence.journal.leveldb-shared.store {
      native = off
      dir = "target/test-shared-journal"
    }
    akka.persistence.snapshot-store.local.dir = "target/test-snapshots"
                                         """))
}

class ReservationSpecMultiJvmNode1 extends ReservationClusterSpec
class ReservationSpecMultiJvmNode2 extends ReservationClusterSpec
class ReservationSpecMultiJvmNode3 extends ReservationClusterSpec

class ReservationClusterSpec extends MultiNodeSpec(ReservationClusterSpec)
  with STMultiNodeSpec with ImplicitSender {

  import ReservationClusterSpec._

  def initialParticipants = roles.size

  val storageLocations = List(
    "akka.persistence.journal.leveldb.dir",
    "akka.persistence.journal.leveldb-shared.store.dir",
    "akka.persistence.snapshot-store.local.dir").map(s => new File(system.settings.config.getString(s)))

  override protected def atStartup() {
    runOn(controller) {
      storageLocations.foreach(dir => FileUtils.deleteDirectory(dir))
    }
  }

  override protected def afterTermination() {
    runOn(controller) {
      storageLocations.foreach(dir => FileUtils.deleteDirectory(dir))
    }
  }

  def join(from: RoleName, to: RoleName): Unit = {
    runOn(from) {
      Cluster(system) join node(to).address
      startSharding()
    }
    enterBarrier(from.name + "-joined")
  }

  def startSharding(): Unit = {
    ClusterSharding(system).start(
      typeName = Reservation.domain,
      entryProps = Some(Props[Reservation]),
      idExtractor = Reservation.idExtractor,
      shardResolver = Reservation.shardResolver)
  }

  "Reservation office" must {

    "setup shared journal" in {
      // start the Persistence extension
      Persistence(system)
      runOn(controller) {
        system.actorOf(Props[SharedLeveldbStore], "store")
      }
      enterBarrier("peristence-started")

      runOn(node1, node2) {
        system.actorSelection(node(controller) / "user" / "store") ! Identify(None)
        val sharedStore = expectMsgType[ActorIdentity].ref.get
        SharedLeveldbJournal.setStore(sharedStore, system)
      }

      enterBarrier("after-1")
    }

    "join cluster" in within(15.seconds) {
      join(node1, node1)
      join(node2, node1)
      enterBarrier("after-2")
    }

    "handle commands from multiply nodes" in within(15.seconds) {
      val reservationId = "reservation1"

      runOn(node1) {
        val reservationOffice = ClusterSharding(system).shardRegion(Reservation.domain)
        reservationOffice ! CreateReservation(reservationId, "client1")
        reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      }

      runOn(node2) {
        val postRegion = ClusterSharding(system).shardRegion(Reservation.domain)
        awaitAssert {
          within(1.second) {
            postRegion ! ReserveProduct(reservationId, "product2", 1)
            expectMsg(Acknowledged)
          }
        }
      }
      enterBarrier("after-3")
    }

  }
}
