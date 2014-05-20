package test.support

import akka.actor._
import akka.testkit.{TestProbe, EventFilter, ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.reflect.ClassTag
import ddd.support.domain.AggregateIdResolution
import scala.util.Failure
import akka.actor.Terminated
import infrastructure.EcommerceSettings

abstract class EventsourcedAggregateRootSpec[T](_system: ActorSystem)(implicit arClassTag: ClassTag[T]) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  val settings = EcommerceSettings(system)
  val domain = arClassTag.runtimeClass.getSimpleName

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
    system.awaitTermination()
  }

  def expectEventPublished[E](implicit t: ClassTag[E]) {
    val probe = TestProbe()
    system.eventStream.subscribe(probe.ref, t.runtimeClass)
    probe.expectMsgClass(2 seconds, t.runtimeClass)
  }

  def expectEventPersisted[E](aggregateId: String)(when: => Unit)(implicit t: ClassTag[E], idResolution: AggregateIdResolution[T]) {
    expectLogMessageFromAR("Event persisted: " + t.runtimeClass.getSimpleName, when)(aggregateId)
  }

  def expectEventPersisted[E](event: E)(aggregateRootId: String)(when: => Unit)(implicit idResolution: AggregateIdResolution[T]) {
    expectLogMessageFromAR("Event persisted: " + event.toString, when)(aggregateRootId)
  }

  def expectLogMessageFromAR(messageStart: String, when: => Unit)(aggregateId: String)(implicit idResolution: AggregateIdResolution[T]) {
    EventFilter.info(
      source = s"akka://Tests/user/$domain/$aggregateId",
      start = messageStart, occurrences = 1)
      .intercept {
      when
    }
  }

  def expectLogMessageFromOffice(messageStart: String)(when: => Unit)(implicit idResolution: AggregateIdResolution[T]) {
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

  def expectReply[O](obj: O) {
    expectMsg(20.seconds, obj)
  }

  def ensureActorTerminated(actor: ActorRef) = {
    watch(actor)
    actor ! PoisonPill
    // wait until reservation office is terminated
    fishForMessage(1.seconds) {
      case Terminated(_) =>
        unwatch(actor)
        true
      case _ => false
    }

  }

}
