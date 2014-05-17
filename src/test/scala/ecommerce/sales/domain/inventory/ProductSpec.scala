package ecommerce.sales.domain.inventory

import akka.actor.{Props, ActorRef}
import ecommerce.sales.domain.reservation.Reservation._
import test.support.{LocalPublisher, EventsourcedAggregateRootSpec}
import ddd.support.domain.Office._
import test.support.TestConfig._
import ecommerce.sales.domain.inventory.Product.{AddProduct, ProductAdded}
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain.AggregateRootActorFactory
import infrastructure.actor.PassivationConfig

object ProductSpec {
  implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
    override def props(config: PassivationConfig) = Props(new Product(config) with LocalPublisher)
  }
}

class ProductSpec extends EventsourcedAggregateRootSpec[Product](testSystem)  {
  import ProductSpec._

  var inventoryOffice: ActorRef = system.deadLetters

  before {
    inventoryOffice = office[Product]
  }

  after {
    ensureActorTerminated(inventoryOffice)
  }

  "Product AR" should {
    "communicate outcome with events" in {
      // given
      val productId = "product-1"

      // then
      expectEventPersisted[ProductAdded](productId) {
        inventoryOffice ! AddProduct(productId, "product 1", ProductType.Standard, Money(10))
      }
    }
  }


}
