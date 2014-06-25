package ecommerce.integration

import akka.actor.Props
import ddd.support.domain.AggregateRootActorFactory
import ddd.support.domain.protocol.Acknowledged
import ecommerce.inventory.domain.Product
import ecommerce.inventory.domain.Product.AddProduct
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.sales.integration.InventoryProjection
import ecommerce.sales.productcatalog.ProductFinder
import ecommerce.sales.sharedkernel.ProductType
import ecommerce.system.infrastructure.events.Projection
import ecommerce.system.infrastructure.office.Office._
import infrastructure.EcommerceSettings
import infrastructure.actor.PassivationConfig
import infrastructure.akka.broker.ActiveMQMessaging
import infrastructure.akka.event.ReliablePublisher
import infrastructure.cluster.ShardingSupport._
import infrastructure.cluster._
import infrastructure.view.ViewDatabase
import test.support.ClusterConfig._
import test.support.ClusterSpec
import test.support.broker.EmbeddedBrokerTestSupport
import test.support.view.{ Daos, ViewsTestSupport }

import scala.slick.driver.H2Driver

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
  }

  val inventoryQueue = system.actorOf(InventoryQueue.props, InventoryQueue.name)

  implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
    override def props(passivationConfig: PassivationConfig): Props = {
      Props(new Product(passivationConfig) with ReliablePublisher {
        override val target = inventoryQueue.path
      })
    }
  }

  "ProductAdded events published from any node" should {
    "be delivered to Product Catalog" in {
      on(node1) {
        val inventoryOffice = office[Product]
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
