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
      office[Product] ! AddProduct("product-1", "product 1", ProductType.Standard, Money(10))
      
      // then
      expectEventPublished[ProductAdded]
    }
  }


}
