package ecommerce.sales.domain.inventory

import akka.actor.ActorRef
import ecommerce.sales.domain.reservation.Reservation._
import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Office._
import test.support.TestConfig._
import ecommerce.sales.domain.inventory.Product.{AddProduct, ProductAdded}
import ecommerce.sales.sharedkernel.Money

class ProductSpec extends EventsourcedAggregateRootSpec[Product](testSystem)  {

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
