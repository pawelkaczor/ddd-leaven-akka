package ecommerce.inventory.domain.product

import test.support.EventsourcedAggregateRootSpec
import test.support.TestConfig._
import ecommerce.inventory.domain.Product.AddProduct
import ecommerce.sales.sharedkernel.ProductType
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.system.infrastructure.events.{ProjectionSpec, Projection}
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.inventory.domain.Product
import ddd.support.domain.protocol.{Published, Acknowledged}
import ddd.support.domain.{SnapshotId, AggregateRootActorFactory}
import infrastructure.actor.PassivationConfig
import akka.actor.Props
import ddd.support.domain.Office._
import ddd.support.domain.event.DomainEvent
import infrastructure.akka.event.ReliablePublisher

class ProductAcknowledgedPublicationSpec extends EventsourcedAggregateRootSpec[Product](testSystem) with EmbeddedBrokerTestSupport {

  "Publication of new product" should {
    "be explicitly acknowledged" in {
      // given
      val inventoryQueuePath = system.actorOf(
        InventoryQueue.recipeForInOut,
        InventoryQueue.name).path

      implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
        override def props(passivationConfig: PassivationConfig): Props = {
          Props(new Product(passivationConfig) with ReliablePublisher {
            override val target = inventoryQueuePath
            override val applicationLevelAck = true
          })
        }
      }

      Projection(InventoryQueue.EndpointUri, new ProjectionSpec() {
        override def apply(id: SnapshotId, event: DomainEvent): Unit = {
          // do nothing
        }
      })

      // when
      office[Product] ! AddProduct("product-1", "product 1", ProductType.Standard)

      // then
      expectReply(Acknowledged)
      expectReply(Published)

    }
  }

}
