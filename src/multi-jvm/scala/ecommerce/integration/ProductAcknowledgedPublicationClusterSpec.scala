package ecommerce.integration

import ecommerce.sales.productcatalog.ProductFinder
import ecommerce.system.infrastructure.office.Office._
import ddd.support.domain.AggregateRootActorFactory
import akka.actor.Props
import test.support.{ ClusterConfig, ClusterSpec }
import test.support.broker.EmbeddedBrokerTestSupport
import ClusterConfig._
import infrastructure.akka.broker.ActiveMQMessaging
import test.support.view.{ Daos, ViewsTestSupport }
import scala.slick.driver.H2Driver
import ecommerce.inventory.domain.Product.{ ProductAdded, AddProduct }
import ecommerce.inventory.domain.Product
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.{ ViewUpdated, Acknowledged }
import infrastructure.view.ViewDatabase
import infrastructure.EcommerceSettings
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.sales.productcatalog.ProductFinder.GetProduct
import ecommerce.sales.sharedkernel.ProductType.Standard
import ecommerce.system.infrastructure.events.Projection
import ecommerce.sales.integration.InventoryProjection
import infrastructure.akka.event.ReliablePublisher
import ecommerce.system.DeliveryContext
import infrastructure.cluster._
import ShardingSupport._

class ProductAcknowledgedPublicationClusterSpecMultiJvmNode1
  extends ProductAcknowledgedPublicationClusterSpec with EmbeddedBrokerTestSupport

class ProductAcknowledgedPublicationClusterSpecMultiJvmNode2
  extends ProductAcknowledgedPublicationClusterSpec with ActiveMQMessaging with ViewsTestSupport

class ProductAcknowledgedPublicationClusterSpec extends ClusterSpec with ViewDatabase {

  override val settings = EcommerceSettings(system)
  lazy val daos = new Daos(H2Driver)
  lazy val inventoryQueue = system.actorOf(InventoryQueue.props, InventoryQueue.name)

  implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
    override def props(config: PassivationConfig): Props = {
      Props(new Product(config) with ReliablePublisher {
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

  "Newly published product" should {
    "be findable as soon as publication is acknowledged" in {

      Projection(InventoryQueue.ExchangeName, new InventoryProjection(viewDb, daos))

      on(node1) {
        // when
        import DeliveryContext.Adjust._
        office[Product] ! AddProduct("product-1", "product 1", Standard).requestDLR[ViewUpdated]

        // then
        expectReply(Acknowledged)
        expectReply(ViewUpdated(ProductAdded("product 1", Standard)))

        enterBarrier("publication acknowledged")
      }

      on(node2) {
        val productFinder = system.actorOf(ProductFinder.props(viewDb, daos), ProductFinder.name)

        enterBarrier("publication acknowledged")

        // when (immediate query possible as publication in product catalog has been already acknowledged)
        productFinder ! GetProduct("product-1")

        // then
        expectReply[Some[Product]]
      }
    }
  }

}

