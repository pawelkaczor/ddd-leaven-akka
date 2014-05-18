package ecommerce.sales.domain.inventory

import ecommerce.sales.domain.reservation.Reservation._

import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Office._
import test.support.TestConfig._
import akka.actor._
import infrastructure.actor.PassivationConfig
import ddd.support.domain.{AggregateRootActorFactory, ReliablePublisher}
import ecommerce.sales.domain.inventory.Product.{AddProduct, ProductAdded}
import ecommerce.sales.sharedkernel.Money
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.sales.infrastructure.inventory.InventoryQueue
import ecommerce.system.infrastructure.events.EventListener

class ProductReliablePublishingSpec extends EventsourcedAggregateRootSpec[Product](testSystem) with EmbeddedBrokerTestSupport {

  "New product" should {
    "be published using inventory queue" in {
      // given
      val inventoryQueuePath = system.actorOf(InventoryQueue.props, name = InventoryQueue.name).path

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
      office[Product] ! AddProduct("product-1", "product 1", ProductType.Standard, Money(10))

      // then
      expectEventPublished[ProductAdded]

    }
  }

}
