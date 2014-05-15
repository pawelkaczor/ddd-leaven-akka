package ecommerce.sales.domain.inventory

import scala.concurrent.duration._
import ddd.support.domain.Office._
import akka.testkit.TestProbe
import scala.reflect.ClassTag
import ddd.support.domain.{ReliablePublisher, AggregateRootActorFactory}
import akka.actor.Props
import test.support.{ClusterConfig, ClusterSpec}
import ecommerce.sales.sharedkernel.Money
import ecommerce.sales.domain.inventory.Product.ProductAdded
import ecommerce.sales.domain.inventory.Product.AddProduct
import infrastructure.actor.PassivationConfig
import ecommerce.sales.infrastructure.inventory.{InventoryQueue, InventoryListener}
import test.support.broker.EmbeddedBrokerTestSupport
import ClusterConfig._
import infrastructure.akka.broker.ActiveMQMessaging

class EventsPublishingClusterSpecMultiJvmNode1
  extends EventsPublishingClusterSpec with EmbeddedBrokerTestSupport

class EventsPublishingClusterSpecMultiJvmNode2
  extends EventsPublishingClusterSpec with ActiveMQMessaging {

  def startInventoryListener() {
    system.actorOf(Props[InventoryListener], name = "inventoryListener")
  }

  override protected def atStartup() {
    super.atStartup()
    startInventoryListener()
  }
  
}

class EventsPublishingClusterSpec extends ClusterSpec  {

  override protected def atStartup() {
    super.atStartup()
    setupSharedJournal()
    joinCluster()
    registerGlobalInventoryOffice()
  }

  def registerGlobalInventoryOffice() {
    val inventoryQueue = system.actorOf(Props[InventoryQueue], name = "inventoryQueue")

    implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
      override def props(passivationConfig: PassivationConfig): Props = {
        Props(new Product(passivationConfig) with ReliablePublisher {
          override val target = inventoryQueue.path
        })
      }
    }

    startSharding[Product]
  }

  "Inventory events published from any node" must {
    enterBarrier("when")

    "be propagated to single endpoint in the cluster" in {
      val inventoryOffice = globalOffice[Product]

      on(node1) {
          inventoryOffice ! AddProduct("product-1", "product 1", ProductType.Standard, Money(10))
          inventoryOffice ! AddProduct("product-2", "product 2", ProductType.Standard, Money(10))

          listenToEvetsOfClass[ProductAdded].expectNoMsg(5.seconds)
      }

      on(node2) {
        val localListener = listenToEvetsOfClass[ProductAdded]
        localListener.expectMsgClass(20.seconds, classOf[ProductAdded])
        localListener.expectMsgClass(20.seconds, classOf[ProductAdded])
      }

    }

  }

  def expectReply[T, R](obj: T)(when: => R): R = {
    val r = when
    expectMsg(20.seconds, obj)
    r
  }

  def listenToEvetsOfClass[E](implicit t: ClassTag[E]) = {
    val probe = TestProbe()
    system.eventStream.subscribe(probe.ref, t.runtimeClass)
    probe
  }
  
  def expectEventPublished[E](implicit t: ClassTag[E]) {
    expectEventPublished(listenToEvetsOfClass[E])()
  }

  def expectEventPublished[E](probe: TestProbe)(when: Unit)(implicit t: ClassTag[E]) {
    val r = when
    probe.expectMsgClass(20.seconds, t.runtimeClass)
    r
  }
}
