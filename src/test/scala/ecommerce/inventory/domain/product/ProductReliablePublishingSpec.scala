package ecommerce.inventory.domain.product

import akka.actor._
import ddd.support.domain.AggregateRootActorFactory
import ecommerce.inventory.domain.Product
import ecommerce.inventory.domain.Product.{ AddProduct, ProductAdded }
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.sales.sharedkernel.ProductType
import ecommerce.system.infrastructure.office.Office._
import infrastructure.actor.PassivationConfig
import infrastructure.akka.event.ReliablePublisher
import test.support.{ LocalOffice, EventsourcedAggregateRootSpec }
import test.support.TestConfig._
import test.support.broker.EmbeddedBrokerTestSupport
import LocalOffice._
import ecommerce.system.infrastructure.events.EventConsumer

class ProductReliablePublishingSpec extends EventsourcedAggregateRootSpec[Product](testSystem) with EmbeddedBrokerTestSupport {

  "New product" should {
    "be published using inventory queue" in {
      // given
      val inventoryQueuePath = system.actorOf(InventoryQueue.props, InventoryQueue.name).path

      implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
        override def props(passivationConfig: PassivationConfig): Props = {
          Props(new Product(passivationConfig) with ReliablePublisher {
            override val target = inventoryQueuePath
          })
        }
      }

      EventConsumer(InventoryQueue.ExchangeName) {
        eventMessage => system.eventStream.publish(eventMessage.event)
      }

      // when
      office[Product] ! AddProduct("product-1", "product 1", ProductType.Standard)

      // then
      expectEventPublished[ProductAdded]

    }
  }

}
