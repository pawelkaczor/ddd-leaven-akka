package ecommerce.inventory.domain.product

import ecommerce.system.infrastructure.office.Office._

import test.support.{ LocalOffice, ReliableLocalPublisher, EventsourcedAggregateRootSpec }
import test.support.TestConfig._
import akka.actor._
import infrastructure.actor.{ CreationSupport, PassivationConfig }
import ddd.support.domain.AggregateRootActorFactory
import ecommerce.inventory.domain.Product.{ AddProduct, ProductAdded }
import ecommerce.sales.sharedkernel.ProductType
import ecommerce.inventory.domain.Product
import infrastructure.akka.event.ReliablePublisher
import LocalOffice._

class ProductPublishingSpec extends EventsourcedAggregateRootSpec[Product](testSystem) {

  def localPublisher(implicit creator: CreationSupport) = {
    creator.getOrCreateChild(Props[ReliableLocalPublisher], name = "localEventPublisher")
  }

  "New product" should {
    "be advertised to configurable destination actor" in {
      // given
      implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
        override def props(config: PassivationConfig): Props = {
          Props(new Product(config) with ReliablePublisher {
            override val target = localPublisher.path
          })
        }
      }

      // when
      office[Product] ! AddProduct("product-1", "product 1", ProductType.Standard)

      // then
      expectEventPublished[ProductAdded]
    }
  }

}
