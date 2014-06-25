package ecommerce.inventory.domain.product

import ecommerce.system.infrastructure.office.Office._
import test.support.{ LocalOffice, EventsourcedAggregateRootSpec }
import test.support.TestConfig._
import ecommerce.inventory.domain.Product.{ ProductAdded, AddProduct }
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.system.infrastructure.events.{ ProjectionSpec, Projection }
import ecommerce.inventory.integration.InventoryQueue
import ecommerce.inventory.domain.Product
import ddd.support.domain.protocol.{ ViewUpdated, Acknowledged }
import ddd.support.domain.AggregateRootActorFactory
import infrastructure.actor.PassivationConfig
import akka.actor.Props
import infrastructure.akka.event.ReliablePublisher
import ecommerce.system.DeliveryContext
import ecommerce.sales.sharedkernel.ProductType.Standard

class ProductAcknowledgedPublicationSpec extends EventsourcedAggregateRootSpec[Product](testSystem)
  with EmbeddedBrokerTestSupport {

  "Publication of new product" should {
    "be explicitly acknowledged" in {
      import LocalOffice._

      // given
      val inventoryQueue = system.actorOf(InventoryQueue.props, InventoryQueue.name)

      implicit object ProductActorFactory extends AggregateRootActorFactory[Product] {
        override def props(passivationConfig: PassivationConfig): Props = {
          Props(new Product(passivationConfig) with ReliablePublisher {
            override val target = inventoryQueue.path
          })
        }
      }

      Projection(InventoryQueue.EndpointUri, mock[ProjectionSpec])

      // when
      import DeliveryContext.Adjust._
      office[Product] ! AddProduct("product-1", "product 1", Standard).requestDLR[ViewUpdated]

      // then
      expectReply(Acknowledged)
      expectReply(ViewUpdated(ProductAdded("product 1", Standard)))

    }
  }

}
