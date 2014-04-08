import akka.actor.{PoisonPill, Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import ddd.domain.sharedkernel.Money
import erp.sales.domain.order.Order._
import erp.sales.domain.order.Order
import erp.sales.domain.order.Order.AddProduct
import erp.sales.domain.order.Order.CreateOrder
import erp.sales.domain.order.Order.OrderCreated
import erp.sales.domain.order.Order.ProductAddedToOrder
import erp.sales.domain.ProductType

import OrderSpec._

object OrderSpec {
  val testSystem = {
    val config = ConfigFactory.parseString(
      """akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.persistence.journal.plugin = "in-memory-journal"
      """.stripMargin)
    ActorSystem("OrderSpec", config)
  }
}

class OrderSpec extends EventsourcedAggregateRootSpec(testSystem) {

  override val aggregateRootId = "order1"

  def getOrderActor(name: String) = {
    getActor(Props[Order])(name)
  }

  "An Order actor" must {
    "handle Order process" in {
      val orderId = aggregateRootId
      var order = getOrderActor(orderId)

      expectEventLogged[OrderCreated] {
        order ! CreateOrder(orderId, "client1")
      }
      expectEventLogged[ProductAddedToOrder] {
        order ! AddProduct(orderId, "product1", 1)
      }

      // kill and recreate order actor
      order ! PoisonPill
      Thread.sleep(1000)
      order = getOrderActor(orderId)

      expectEventLogged(ProductAddedToOrder("product2", orderId, ProductType.Standard, Money(10), 2)) {
        order ! AddProduct(orderId, "product2", 2)
      }

      expectEventLogged[OrderArchived] {
        order ! ArchiveOrder(orderId)
      }

    }
  }

}
