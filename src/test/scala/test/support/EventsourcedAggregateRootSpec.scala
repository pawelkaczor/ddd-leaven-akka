package test.support

import akka.actor._
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import akka.util.Timeout
import ddd.support.domain.event.DomainEvent
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.Await

abstract class EventsourcedAggregateRootSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val aggregateRootId: String

  val parentName = "parent"
  val parent: ActorRef = system.actorOf(Props(new Actor with ActorContextCreationSupport {
    def receive = {
      case ("getOrCreateChild", props:Props, name:String) => sender() ! getOrCreateChild(props, name)
    }
  }), name = parentName)

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  import akka.pattern.ask
  import scala.concurrent.duration._

  def getActor(props:Props)(implicit name: String = aggregateRootId): ActorRef = {
    implicit val timeout = Timeout(5, SECONDS)
    Await.result(parent ? ("getOrCreateChild", props, name), 5 seconds).asInstanceOf[ActorRef]
  }

  def expectEventPersisted[E <: DomainEvent](when: Unit)(implicit m: Manifest[E]) {
    val eventPersistedMsg = "Event persisted: " + m.runtimeClass.getSimpleName
    EventFilter.info(
      source = s"akka://OrderSpec/user/$parentName/$aggregateRootId",
      start = eventPersistedMsg, occurrences = 1)
      .intercept {
      when
    }
  }

  def expectEventPersisted[E <: DomainEvent](event: E)(when: Unit) {
    val eventPersistedMsg = "Event persisted: " + event.toString
    EventFilter.info(
      source = s"akka://OrderSpec/user/$parentName/$aggregateRootId",
      start = eventPersistedMsg, occurrences = 1)
      .intercept {
      when
    }
  }

}
