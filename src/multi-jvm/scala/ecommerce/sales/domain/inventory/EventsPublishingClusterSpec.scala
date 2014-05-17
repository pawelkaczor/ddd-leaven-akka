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
import ecommerce.sales.infrastructure.inventory.InventoryQueue
import test.support.broker.EmbeddedBrokerTestSupport
import ClusterConfig._
import infrastructure.akka.broker.ActiveMQMessaging
import ecommerce.sales.view.inventory.ProductCatalog
import test.support.view.ViewsTestSupport

class EventsPublishingClusterSpecMultiJvmNode1
  extends EventsPublishingClusterSpec with EmbeddedBrokerTestSupport

class EventsPublishingClusterSpecMultiJvmNode2
  extends EventsPublishingClusterSpec with ActiveMQMessaging with ViewsTestSupport {

  def startProductCatalog = system.actorOf(ProductCatalog.props(db, dal), name = "productCatalog")

  override protected def atStartup() {
    super.atStartup()
    startProductCatalog
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
    val inventoryQueue = system.actorOf(InventoryQueue.props, InventoryQueue.name)

    implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
      override def props(passivationConfig: PassivationConfig): Props = {
        Props(new Product(passivationConfig) with ReliablePublisher {
          override val target = inventoryQueue.path
        })
      }
    }

    startSharding[Product]
  }

  "Inventory events published from any node" should {
    enterBarrier("when")

    "be propagated to single endpoint in the cluster" in {
      val inventoryOffice = globalOffice[Product]

      on(node1) {
          inventoryOffice ! AddProduct("product-1", "product 1", ProductType.Standard, Money(10))
          inventoryOffice ! AddProduct("product-2", "product 2", ProductType.Standard, Money(10))

          listenToEventsOfClass[ProductAdded].expectNoMsg(5.seconds)
      }

      on(node2) {
        val localListener = listenToEventsOfClass[ProductAdded]
        localListener.expectMsgClass(20.seconds, classOf[ProductAdded])
        localListener.expectMsgClass(20.seconds, classOf[ProductAdded])
      }

    }

  }

  def listenToEventsOfClass[E](implicit t: ClassTag[E]) = {
    val probe = TestProbe()
    system.eventStream.subscribe(probe.ref, t.runtimeClass)
    probe
  }
  
}
