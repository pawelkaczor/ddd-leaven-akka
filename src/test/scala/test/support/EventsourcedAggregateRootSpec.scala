package test.support

import akka.actor._
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import akka.util.Timeout
import ddd.support.domain.event.DomainEvent
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Failure
import scala.reflect.ClassTag
import ddd.support.domain.Addressable

abstract class EventsourcedAggregateRootSpec[T](_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
    system.awaitTermination()
  }

  def expectEventPersisted[E <: DomainEvent](aggregateRootId: String)(when: Unit)(implicit t: ClassTag[E], addressable: Addressable[T]) {
    expectLogMessageFromAR("Event persisted: " + t.runtimeClass.getSimpleName, when)(aggregateRootId)
  }

  def expectEventPersisted[E <: DomainEvent](event: E)(aggregateRootId: String)(when: Unit)(implicit addressable: Addressable[T]) {
    expectLogMessageFromAR("Event persisted: " + event.toString, when)(aggregateRootId)
  }

  def expectLogMessageFromAR(messageStart: String, when: Unit)(aggregateRootId: String)(implicit addressable: Addressable[T]) {
    val domain = addressable.domain
    EventFilter.info(
      source = s"akka://Tests/user/$domain/$aggregateRootId",
      start = messageStart, occurrences = 1)
      .intercept {
      when
    }
  }

  def expectLogMessageFromOffice(messageStart: String)(when: Unit)(implicit addressable: Addressable[T]) {
    val domain = addressable.domain
    EventFilter.info(
      source = s"akka://Tests/user/$domain",
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

  def expectReply[O, R](obj: O)(when: => R): R = {
    val r = when
    expectMsg(20.seconds, obj)
    r
  }

}
