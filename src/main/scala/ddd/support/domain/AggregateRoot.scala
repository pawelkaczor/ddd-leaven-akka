package ddd.support.domain

import akka.actor._
import akka.persistence._
import infrastructure.actor.GracefulPassivation
import ddd.support.domain.error.AggregateRootNotInitializedException
import AggregateRoot.Event
import ddd.support.domain.event.{EventMessage, DomainEventMessage, EventHandler, DomainEvent}
import akka.actor.Status.Failure
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.Acknowledged
import ddd.support.domain.command.CommandMessage

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
  private var _lastCommandMessage: Option[CommandMessage] = None
  val factory: AggregateRootFactory

  override def processorId: String = aggregateId

  override def receiveCommand: Receive = {
    case cm: CommandMessage =>
      _lastCommandMessage = Some(cm)
      handleCommand.applyOrElse(cm.command, unhandled)
  }

  override def receiveRecover: Receive = {
    case event: EventMessage =>
      updateState(event.payload)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }

  def commandMessage = _lastCommandMessage.get

  def aggregateId = self.path.name

  def handleCommand: Receive

  def updateState(event: Event) {
    val nextState = if (initialized) state.apply(event) else factory.apply(event)
    stateOpt = Option(nextState.asInstanceOf[S])
  }

  def raise(event: Event) {
    persist(new EventMessage(payload = event, metaData = commandMessage.metaData)) {
      persisted => {
        log.info("Event persisted: {}", event)
        updateState(event)
        handle(toDomainEventMessage(persisted))
      }
    }
  }

  def toDomainEventMessage(persisted: EventMessage) =
    new DomainEventMessage(persisted, SnapshotId(aggregateId, lastSequenceNr))

  override def handle(event: DomainEventMessage) {
    sender ! Acknowledged
  }

  def initialized = stateOpt.isDefined

  def state = if (initialized) stateOpt.get else throw new AggregateRootNotInitializedException

}
