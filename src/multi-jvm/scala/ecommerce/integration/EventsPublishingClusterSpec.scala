package ecommerce.integration

import akka.actor.Props
import ddd.support.domain.AggregateRootActorFactory
import ddd.support.domain.protocol.Acknowledged
import ecommerce.inventory.domain.Product
import ecommerce.inventory.domain.Product.AddProduct
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.sales.integration.InventoryProjection
import ecommerce.sales.productcatalog.ProductFinder
import ecommerce.sales.productcatalog.ProductFinder.GetProduct
import ecommerce.sales.sharedkernel.ProductType.Standard
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
  lazy val daos = new Daos(H2Driver)
  lazy val inventoryQueue = system.actorOf(InventoryQueue.props, InventoryQueue.name)

  implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
    override def props(pc: PassivationConfig): Props = {
      Props(new Product(pc) with ReliablePublisher {
        override val target = inventoryQueue.path
      })
    }
  }

  override protected def atStartup() {
    super.atStartup()
    setupSharedJournal()
    joinCluster()
    // start shard region for Product on each node
    office[Product]
  }

  "ProductAdded events published from any node" should {
    "be delivered to Product Catalog" in {
      Projection(InventoryQueue.ExchangeName, new InventoryProjection(viewDb, daos))

      on(node1) {
        office[Product] ! AddProduct("product-1", "product 1", Standard)
        expectReply(Acknowledged)

        office[Product] ! AddProduct("product-2", "product 2", Standard)
        expectReply(Acknowledged)

        enterBarrier("events published")
      }

      on(node2) {
        val productFinder = system.actorOf(ProductFinder.props(viewDb, daos), ProductFinder.name)

        enterBarrier("events published")

        // Acknowledgment of publication of new products in catalog has not been requested, thus
        // immediate query might fail
        Thread.sleep(1000)

        productFinder ! GetProduct("product-1")
        expectReply[Some[Product]]

        productFinder ! GetProduct("product-2")
        expectReply[Some[Product]]

      }

    }
  }

}
