package ecommerce.integration

import ecommerce.sales.productcatalog.ProductFinder
import ddd.support.domain.Office._
import ddd.support.domain.AggregateRootActorFactory
import akka.actor.Props
import test.support.{ClusterConfig, ClusterSpec}
import test.support.broker.EmbeddedBrokerTestSupport
import ClusterConfig._
import infrastructure.akka.broker.ActiveMQMessaging
import test.support.view.{Daos, ViewsTestSupport}
import scala.slick.driver.H2Driver
import ecommerce.inventory.domain.Product.AddProduct
import ecommerce.inventory.domain.Product
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.{ViewUpdated, Acknowledged}
import infrastructure.view.ViewDatabase
import infrastructure.EcommerceSettings
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.sales.productcatalog.ProductFinder.GetProduct
import ecommerce.sales.sharedkernel.ProductType.Standard
import ecommerce.system.infrastructure.events.Projection
import ecommerce.sales.integration.InventoryProjection
import infrastructure.akka.event.ReliablePublisher
import ecommerce.system.DeliveryContext

class ProductAcknowledgedPublicationClusterSpecMultiJvmNode1
  extends ProductAcknowledgedPublicationClusterSpec with EmbeddedBrokerTestSupport

class ProductAcknowledgedPublicationClusterSpecMultiJvmNode2
  extends ProductAcknowledgedPublicationClusterSpec with ActiveMQMessaging with ViewsTestSupport


class ProductAcknowledgedPublicationClusterSpec extends ClusterSpec with ViewDatabase {

  override val settings = EcommerceSettings(system)

  override protected def atStartup() {
    super.atStartup()
    setupSharedJournal()
    joinCluster()
    registerGlobalInventoryOffice()
  }

  def registerGlobalInventoryOffice() {
    val inventoryQueue = system.actorOf(InventoryQueue.recipeForInOut, InventoryQueue.name)

    implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
      override def props(config: PassivationConfig): Props = {
        Props(new Product(config) with ReliablePublisher {
          override val target = inventoryQueue.path
        })
      }
    }
    startSharding[Product]
  }

  "Newly published product" should {
    "be findable as soon as publication is acknowledged" in {
      on(node1) {
        // given
        val inventoryOffice = globalOffice[Product]

        // when
        import DeliveryContext.Adjust._
        inventoryOffice ! AddProduct("product-1", "product 1", Standard).requestDLR()

        // then
        expectReply(Acknowledged)
        expectReply(ViewUpdated)

        enterBarrier("publication acknowledged")
      }

      on(node2) {
        val daos = new Daos(H2Driver)
        Projection(InventoryQueue.EndpointUri, new InventoryProjection(viewDb, daos))
        val productFinder = system.actorOf(ProductFinder.props(viewDb, daos), ProductFinder.name)

        // given
        enterBarrier("publication acknowledged")

        // when (immediate query possible as publication in product catalog has been already acknowledged)
        productFinder ! GetProduct("product-1")

        // then
        expectReply[Some[Product]]
      }
    }
  }

}

