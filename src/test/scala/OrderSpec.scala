import akka.actor.{Props, ActorSystem}
import akka.testkit.{EventFilter, TestKit, ImplicitSender}
import com.typesafe.config.ConfigFactory
import ddd.domain.event.DomainEvent
import erp.sales.domain.order.Order.{AddProduct, ProductAddedToOrder, OrderCreated, CreateOrder}
import erp.sales.domain.order.Order
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

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

class OrderSpec extends TestKit(testSystem)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An Order actor" must {
    "accept CreateOrder" in {
      val order = system.actorOf(Props[Order], name = "order")
      expectEvent(classOf[OrderCreated]) {
        order ! CreateOrder("order1", "client1")
      }
      expectEvent(classOf[ProductAddedToOrder]) {
        order ! AddProduct("order1", "product1", 1)
      }
    }
  }

  def expectEvent[T <: DomainEvent](eventClass: Class[T])(when: Unit) {
    val eventAppliedMsg = ".+" + eventClass.getSimpleName
    EventFilter.info(
      source = "akka://OrderSpec/user/order",
      pattern = eventAppliedMsg, occurrences = 1)
      .intercept {
        when
    }
  }
}
