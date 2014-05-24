package ddd.support.domain

import akka.actor._
import akka.persistence._
import infrastructure.actor.GracefulPassivation
import ddd.support.domain.error.AggregateRootNotInitializedException
import AggregateRoot.Event
import ddd.support.domain.event.{EventHandler, DomainEvent}
import akka.actor.Status.Failure
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.Acknowledged

object AggregateRoot {
  type Event = DomainEvent
}

trait AggregateState {
  type StateMachine = PartialFunction[Event, AggregateState]
  def apply: StateMachine
}

abstract class AggregateRootActorFactory[T <: AggregateRoot[_]] {
  def props(passivationConfig: PassivationConfig): Props
}

trait AggregateRoot[S <: AggregateState]
  extends GracefulPassivation with EventsourcedProcessor with EventHandler with ActorLogging {

  type AggregateRootFactory = PartialFunction[Event, S]
  private var stateOpt: Option[S] = None
  val factory: AggregateRootFactory

  override def processorId: String = aggregateId

  override def receiveCommand: Receive = {
    case msg =>
      handleCommand.applyOrElse(msg, unhandled)
  }

  override def receiveRecover: Receive = {
    case event: Event =>
      updateState(event)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }

  def aggregateId = self.path.name

  def handleCommand: Receive

  def updateState(event: Event) {
    val nextState = if (initialized) state.apply(event) else factory.apply(event)
    stateOpt = Option(nextState.asInstanceOf[S])
  }

  def raise(event: Event) {
    persist(event) {
      persistedEvent => {
        log.info("Event persisted: {}", event)
        updateState(persistedEvent)
        handle(persistedEvent)
      }
    }
  }

  override def handle(event: Event) {
    sender ! Acknowledged
  }

  def initialized = stateOpt.isDefined

  def state = if (initialized) stateOpt.get else throw new AggregateRootNotInitializedException

}
