package ecommerce.sales.domain.inventory

import ecommerce.sales.domain.reservation.Reservation._
import scala.concurrent.duration._

import test.support.{EventLocalConfirmablePublisher, EventsourcedAggregateRootSpec}
import ddd.support.domain.Office._
import test.support.TestConfig._
import akka.actor._
import infrastructure.actor.PassivationConfig
import ddd.support.domain.{AggregateRootActorFactory, ReliablePublisher}
import ecommerce.sales.domain.inventory.Product.{AddProduct, ProductAdded}
import ecommerce.sales.sharedkernel.Money

class ProductPublishingSpec extends EventsourcedAggregateRootSpec[Product](testSystem)  {

  def localPublisher(implicit context: ActorContext) = {
    context.system.actorOf(Props[EventLocalConfirmablePublisher], name="localEventPublisher")
  }

  implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
    override def props(passivationConfig: PassivationConfig): Props = {
      Props(new Product(passivationConfig) with ReliablePublisher {
        override val target = localPublisher.path
      })
    }
  }

  "New product" must {
    "be advertised to configurable destination actor" in {
      val productId = "product-1"
      val inventoryOffice = office[Product]

      expectEventPublished[ProductAdded] {
        inventoryOffice ! AddProduct(productId, "product 1", ProductType.Standard, Money(10))
      }

    }
  }


}
