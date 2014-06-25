package test.support

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import java.io.File
import akka.actor.{ Identify, Props, ActorIdentity }
import akka.persistence.journal.leveldb.{ SharedLeveldbStore, SharedLeveldbJournal }
import akka.persistence.Persistence
import akka.remote.testconductor.RoleName
import akka.cluster.Cluster
import org.apache.commons.io.FileUtils
import scala.reflect.ClassTag
import scala.concurrent.duration._

abstract class ClusterSpec extends MultiNodeSpec(ClusterConfig)
  with STMultiNodeSpec with ImplicitSender {

  import ClusterConfig._

  implicit val logger = system.log

  def initialParticipants = roles.size

  val storageLocations = List(
    "akka.persistence.journal.leveldb.dir",
    "akka.persistence.journal.leveldb-shared.store.dir",
    "akka.persistence.snapshot-store.local.dir").map(s => new File(system.settings.config.getString(s)))

  override protected def atStartup() {
    on(node1) {
      storageLocations.foreach(dir => FileUtils.deleteDirectory(dir))
    }
  }

  override protected def afterTermination() {
    on(node1) {
      storageLocations.foreach(dir => FileUtils.deleteDirectory(dir))
    }
  }

  def join(startOn: RoleName, joinTo: RoleName) {
    on(startOn) {
      Cluster(system) join node(joinTo).address
    }
    enterBarrier(startOn.name + "-joined")
  }

  def setupSharedJournal() {
    Persistence(system)
    on(node1) {
      system.actorOf(Props[SharedLeveldbStore], "store")
    }
    enterBarrier("persistence-started")

    system.actorSelection(node(node1) / "user" / "store") ! Identify(None)
    val sharedStore = expectMsgType[ActorIdentity].ref.get
    SharedLeveldbJournal.setStore(sharedStore, system)

    enterBarrier("after-1")
  }

  def joinCluster() {
    join(startOn = node1, joinTo = node1)
    join(startOn = node2, joinTo = node1)
    enterBarrier("after-2")
  }

  def on(nodes: RoleName*)(thunk: ⇒ Unit): Unit = {
    runOn(nodes: _*)(thunk)
  }

  def expectReply[T](obj: T) {
    expectMsg(20.seconds, obj)
  }

  def expectReply[T](implicit tag: ClassTag[T]) {
    expectMsgClass(20.seconds, tag.runtimeClass)
  }

}
