package ddd.support.domain

import akka.actor._
import akka.persistence.{Persistent, Deliver, Channel, EventsourcedProcessor}
import infrastructure.actor.GracefulPassivation
import ddd.support.domain.protocol.Acknowledged
import ddd.support.domain.error.AggregateRootNotInitializedException
import AggregateRoot.Event
import ddd.support.domain.event.{DomainEventMessage, DomainEvent}
import akka.actor.Status.Failure
import infrastructure.actor.PassivationConfig

object AggregateRoot {
  type Event = DomainEvent
}

trait EventPublisher extends EventsourcedProcessor {
  type TargetType
  val target: TargetType

  def publish(event: Event)
}

trait ReliablePublisher extends EventPublisher {
  this: AggregateRoot[_] =>
  
  override type TargetType = ActorPath
  val channel = context.actorOf(Channel.props("publishChannel"))

  abstract override def receiveRecover: Receive = {
    super.receiveRecover.compose(publish).asInstanceOf[Receive]
  }

  override def publish(event: Event) {
    channel ! Deliver(Persistent(DomainEventMessage(aggregateId, event)), target)
  }

}

trait AggregateState {
  type StateMachine = PartialFunction[Event, AggregateState]
  def apply: StateMachine
}

abstract class AggregateRootActorFactory[T <: AggregateRoot[_]] {
  def props(passivationConfig: PassivationConfig): Props
}

trait AggregateRoot[S <: AggregateState]
  extends GracefulPassivation with EventsourcedProcessor with ActorLogging {
  this: EventPublisher =>

  type AggregateRootFactory = PartialFunction[Event, S]
  type EventHandler = Event => Unit
  private var stateOpt: Option[S] = None

  val factory: AggregateRootFactory

  def aggregateId = processorId.split('/').last

  override def receiveCommand: Receive = {
    case msg =>
      handleCommand.applyOrElse(msg, unhandled)
  }

  def handleCommand: Receive

  override def receiveRecover: Receive = {
    case event: Event => 
      updateState(event)
      publish(event) // publisher will check if event has been already published  
  }

  def updateState(event: Event) {
    val nextState = if (initialized) state.apply(event) else factory.apply(event)
    stateOpt = Option(nextState.asInstanceOf[S])
  }

  def raise(event: Event)(implicit handler: EventHandler = handle) {
    persist(event) {
      persistedEvent => {
        log.info("Event persisted: {}", event)
        updateState(persistedEvent)
        handler(persistedEvent)
      }
    }
  }

  def handle(event: Event) {
    publish(event)
    sender() ! Acknowledged
  }

  def initialized = stateOpt.isDefined

  protected def state = if (initialized) stateOpt.get else throw new AggregateRootNotInitializedException


  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }

}
