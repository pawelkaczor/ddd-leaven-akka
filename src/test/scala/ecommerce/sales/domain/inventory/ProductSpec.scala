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
    override def props(passivationConfig: PassivationConfig): Props = Props(new Product(passivationConfig) with LocalPublisher)
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

  "Product AR" must {
    "communicate outcome with events" in {

      val productId = "product-1"
      expectEventPersisted[ProductAdded](productId) {
        inventoryOffice ! AddProduct(productId, "product 1", ProductType.Standard, Money(10))
      }
    }
  }


}
