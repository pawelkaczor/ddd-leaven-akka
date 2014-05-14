package ecommerce.sales.domain.inventory

import ecommerce.sales.domain.reservation.Reservation._

import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Office._
import test.support.TestConfig._
import akka.actor._
import infrastructure.actor.PassivationConfig
import ddd.support.domain.ReliablePublishing
import ecommerce.sales.domain.inventory.Product.{AddProduct, ProductAdded, ProductActorFactory}
import ecommerce.sales.sharedkernel.Money
import akka.camel.CamelExtension
import org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.sales.infrastructure.{InventoryListener, InventoryQueue}

class ProductReliablePublishingSpec extends EventsourcedAggregateRootSpec[Product](testSystem) with EmbeddedBrokerTestSupport {

  val camel = CamelExtension(system)

  val activeMQComp = activeMQComponent("nio://0.0.0.0:61616")
  activeMQComp.setDeliveryPersistent(false)
  camel.context.addComponent("activemq", activeMQComp)

  system.actorOf(Props[InventoryListener], name = "inventoryListener")
  val inventoryQueue = system.actorOf(Props[InventoryQueue], name = "inventoryQueue")

  implicit object ProductActorFactory extends ProductActorFactory {
    override def props(passivationConfig: PassivationConfig): Props = {
      Props(new Product(passivationConfig) with ReliablePublishing {
        override val publisher = inventoryQueue.path
      })
    }
  }

  "New product" must {
    "be published to inventory queue" in {
      val productId = "product-1"
      val inventoryOffice = office[Product]

      expectEventPublished[ProductAdded] {
        inventoryOffice ! AddProduct(productId, "product 1", ProductType.Standard, Money(10))
      }

    }
  }

}
