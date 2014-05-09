package ecommerce.sales.domain.inventory

import ecommerce.sales.domain.reservation.Reservation._
import scala.concurrent.duration._

import test.support.{LocalEventPublisher, EventsourcedAggregateRootSpec}
import ddd.support.domain.Office._
import test.support.TestConfig._
import akka.actor._
import scala.reflect.ClassTag
import akka.testkit.TestProbe
import infrastructure.actor.PassivationConfig
import ddd.support.domain.ReliablePublishing
import ecommerce.sales.domain.inventory.Product.{AddProduct, ProductAdded, ProductActorFactory}
import ecommerce.sales.sharedkernel.Money

class ProductPublishingSpec extends EventsourcedAggregateRootSpec[Product](testSystem)  {

  def localPublisher(implicit context: ActorContext) = {
    context.system.actorOf(Props[LocalEventPublisher], name="localEventPublisher")
  }

  implicit object ProductActorFactory extends ProductActorFactory {
    override def props(passivationConfig: PassivationConfig): Props = {
      Props(new Product(passivationConfig) with ReliablePublishing {
        override val publisher = localPublisher.path
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

  def expectEventPublished[E](when: Unit)(implicit t: ClassTag[E]) {
    val probe = TestProbe()
    system.eventStream.subscribe(probe.ref, t.runtimeClass)
    val r = when
    probe.expectMsgClass(1.seconds, t.runtimeClass)
    r
  }

}
