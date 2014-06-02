package ecommerce.inventory.domain.product

import test.support.{TestConfig, EventsourcedAggregateRootSpec}
import test.support.TestConfig._
import ecommerce.inventory.domain.Product.AddProduct
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.system.infrastructure.events.{ProjectionSpec, Projection}
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.inventory.domain.Product
import ddd.support.domain.protocol.{ViewUpdated, Acknowledged}
import ddd.support.domain.AggregateRootActorFactory
import infrastructure.actor.PassivationConfig
import akka.actor.Props
import ddd.support.domain.Office._
import ddd.support.domain.event.DomainEventMessage
import infrastructure.akka.event.{RedeliveryFailedException, ReliablePublisher}
import ecommerce.system.DeliveryContext
import scala.concurrent.duration._
import ecommerce.sales.sharedkernel.ProductType.Standard
import com.typesafe.config.ConfigFactory
import ProductAcknowledgedPublicationSpec._
import org.mockito.BDDMockito
import org.mockito.Matchers.isA

object ProductAcknowledgedPublicationSpec {
  val customConf = ConfigFactory.parseString("""
      akka.test.filter-leeway = 20s
      """).withFallback(TestConfig.config)
}

class ProductAcknowledgedPublicationSpec extends EventsourcedAggregateRootSpec[Product](testSystem(customConf))
  with EmbeddedBrokerTestSupport {

  "Publication of new product" should {
    "be eventually explicitly acknowledged" in {
      // given
      activeMQComp.setRequestTimeout(500)
      val inventoryQueue = system.actorOf(InventoryQueue.recipeForInOut, InventoryQueue.name)

      implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
        override def props(passivationConfig: PassivationConfig): Props = {
          Props(new Product(passivationConfig) with ReliablePublisher {
            override val target = inventoryQueue.path
            override val redeliverInterval = 1.seconds
            override val redeliverMax = 2
          })
        }
      }

      val projection = mock[ProjectionSpec]
      def exception(counter: Int) = new RuntimeException(s"Test projection failed for the $counter time.")
      BDDMockito.given(projection.apply(isA(classOf[DomainEventMessage])))
        .willThrow(exception(1))
        .willThrow(exception(2))
        .willThrow(exception(3))
        .willThrow(exception(4))
        .willCallRealMethod()

      Projection(InventoryQueue.EndpointUri, projection)

      // when
      import DeliveryContext.Adjust._
      office[Product] ! AddProduct("product-1", "product 1", Standard).requestDLR()


      // then
      expectReply(Acknowledged)
      expectExceptionLogged[RedeliveryFailedException]()
      expectReply(ViewUpdated)

    }
  }

}
