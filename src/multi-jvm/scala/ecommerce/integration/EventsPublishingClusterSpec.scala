package ecommerce.integration

import ddd.support.domain.Office._
import ddd.support.domain.AggregateRootActorFactory
import akka.actor.Props
import test.support.{ ClusterConfig, ClusterSpec }
import ecommerce.sales.sharedkernel.ProductType
import test.support.broker.EmbeddedBrokerTestSupport
import ClusterConfig._
import infrastructure.akka.broker.ActiveMQMessaging
import test.support.view.{ Daos, ViewsTestSupport }
import scala.slick.driver.H2Driver
import ecommerce.inventory.domain.Product.AddProduct
import ecommerce.inventory.domain.Product
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.Acknowledged
import infrastructure.view.ViewDatabase
import infrastructure.EcommerceSettings
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.sales.productcatalog.ProductFinder
import infrastructure.akka.event.ReliablePublisher
import ecommerce.system.infrastructure.events.Projection
import ecommerce.sales.integration.InventoryProjection

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
        inventoryOffice ! AddProduct("product-1", "product 1", ProductType.Standard)
        expectReply(Acknowledged)

        inventoryOffice ! AddProduct("product-2", "product 2", ProductType.Standard)
        expectReply(Acknowledged)

        enterBarrier("events published")
      }

      on(node2) {
        enterBarrier("events published")

        val daos = new Daos(H2Driver)
        Projection(InventoryQueue.EndpointUri, new InventoryProjection(viewDb, daos))
        val productFinder = system.actorOf(ProductFinder.props(viewDb, daos), ProductFinder.name)

        // Acknowledgment of publication of new products in catalog has not been requested, thus
        // immediate query might fail.
        Thread.sleep(1000)

        productFinder ! ProductFinder.GetProduct("product-1")
        expectReply[Some[Product]]

        productFinder ! ProductFinder.GetProduct("product-2")
        expectReply[Some[Product]]

      }
    }
  }

}
