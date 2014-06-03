package ecommerce.inventory.domain.product

import ecommerce.sales.domain.reservation.Reservation._

import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Office._
import test.support.TestConfig._
import akka.actor._
import infrastructure.actor.PassivationConfig
import ddd.support.domain.AggregateRootActorFactory
import ecommerce.inventory.domain.Product.{AddProduct, ProductAdded}
import ecommerce.sales.sharedkernel.ProductType
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.system.infrastructure.events.EventListener
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.inventory.domain.Product
import infrastructure.akka.event.ReliablePublisher

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

      EventListener(InventoryQueue.EndpointUri) {
        eventMessage => system.eventStream.publish(eventMessage.payload)
      }

      // when
      office[Product] ! AddProduct("product-1", "product 1", ProductType.Standard)

      // then
      expectEventPublished[ProductAdded]

    }
  }

}
