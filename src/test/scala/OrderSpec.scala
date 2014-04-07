import akka.actor.{Props, ActorSystem}
import akka.testkit.{EventFilter, TestKit, ImplicitSender}
import com.typesafe.config.ConfigFactory
import erp.sales.domain.order.Order
import Order.CreateOrder
import erp.sales.domain.order.Order
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class OrderSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  val testSystem = {
    val config = ConfigFactory.parseString(
      """akka.event-handlers = ["akka.testkit.TestEventListener"]""")
    ActorSystem("OrderSpec", config)
  }
  //def this() = this(testSystem)
  def this() = this(ActorSystem("OrderSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Order actor" must {
    "accept CreateOrder" in {
      val order = system.actorOf(Props[Order], name = "order6")

      order ! CreateOrder("1", "client1")
      expectMsg("accepted")
    }

  }
}
