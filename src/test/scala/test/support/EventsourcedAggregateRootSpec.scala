package test.support

import akka.actor._
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import akka.util.Timeout
import ddd.support.domain.event.DomainEvent
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Failure
import scala.reflect.ClassTag
import ddd.support.domain.Addressable

abstract class EventsourcedAggregateRootSpec[T](_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  val aggregateRootId: String

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  def expectEventPersisted[E <: DomainEvent](when: Unit)(implicit t: ClassTag[E], addressable: Addressable[T]) {
    expectLogMessageFromAR("Event persisted: " + t.runtimeClass.getSimpleName, when)
  }

  def expectEventPersisted[E <: DomainEvent](event: E)(when: Unit)(implicit addressable: Addressable[T]) {
    expectLogMessageFromAR("Event persisted: " + event.toString, when)
  }

  def expectLogMessageFromAR(messageStart: String, when: Unit)(implicit addressable: Addressable[T]) {
    val domain = addressable.domain
    EventFilter.info(
      source = s"akka://OrderSpec/user/$domain/$aggregateRootId",
      start = messageStart, occurrences = 1)
      .intercept {
      when
    }
  }

  def expectFailure[E](awaitable: Future[Any])(implicit t: ClassTag[E]) {
    implicit val timeout = Timeout(5, SECONDS)
    val future = Await.ready(awaitable, timeout.duration).asInstanceOf[Future[Any]]
    val futureValue = future.value.get
    futureValue match {
      case Failure(ex) if ex.getClass.equals(t.runtimeClass) => () //ok
      case x => fail(s"Unexpected result: $x")
    }

  }
}
