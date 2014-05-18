package ecommerce.sales.domain.inventory

import scala.concurrent.duration._
import ddd.support.domain.Office._
import scala.reflect.ClassTag
import ddd.support.domain.{ReliablePublisher, AggregateRootActorFactory}
import akka.actor.Props
import test.support.{ClusterConfig, ClusterSpec}
import ecommerce.sales.sharedkernel.Money
import ecommerce.sales.infrastructure.inventory.InventoryQueue
import test.support.broker.EmbeddedBrokerTestSupport
import ClusterConfig._
import infrastructure.akka.broker.ActiveMQMessaging
import ecommerce.sales.view.inventory.ProductCatalog
import test.support.view.{Daos, ViewsTestSupport}
import scala.slick.driver.H2Driver
import ecommerce.sales.domain.inventory.Product.AddProduct
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.Acknowledged
import infrastructure.view.ViewDatabase
import infrastructure.EcommerceSettings

class EventsPublishingClusterSpecMultiJvmNode1
  extends EventsPublishingClusterSpec with EmbeddedBrokerTestSupport

class EventsPublishingClusterSpecMultiJvmNode2
  extends EventsPublishingClusterSpec with ActiveMQMessaging with ViewsTestSupport


class EventsPublishingClusterSpec extends ClusterSpec with ViewDatabase {

  override val settings = EcommerceSettings(system)

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

  "ProductAdded events published from any node" should {
    "be delivered to Product Catalog" in {
      on(node1) {
          val inventoryOffice = globalOffice[Product]
          inventoryOffice ! AddProduct("product-1", "product 1", ProductType.Standard, Money(10))
          expectReply(Acknowledged)

          inventoryOffice ! AddProduct("product-2", "product 2", ProductType.Standard, Money(10))
          expectReply(Acknowledged)

          enterBarrier("events published")
      }

      on(node2) {
        enterBarrier("events published")

        val daos = new Daos(H2Driver)
        val productCatalog = system.actorOf(ProductCatalog.props(viewDb, daos), ProductCatalog.name)

        Thread.sleep(1000) // ProductCatalog must read events from InventoryQueue
        productCatalog ! ProductCatalog.GetProduct("product-1")
        expectReply[Some[ProductData]]

        productCatalog ! ProductCatalog.GetProduct("product-2")
        expectReply[Some[ProductData]]

      }
    }
  }

  def expectReply[T](obj: T)  {
    expectMsg(20.seconds, obj)
  }

  def expectReply[T](implicit tag: ClassTag[T])  {
    expectMsgClass(20.seconds, tag.runtimeClass)
  }


}
