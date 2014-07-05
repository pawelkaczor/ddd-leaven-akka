package ddd.support.domain

import akka.actor._
import akka.persistence._
import infrastructure.actor.GracefulPassivation
import ddd.support.domain.error.AggregateRootNotInitializedException
import AggregateRoot.Event
import ddd.support.domain.event.{ EventMessage, DomainEventMessage, EventHandler, DomainEvent }
import akka.actor.Status.Failure
import infrastructure.actor.PassivationConfig
import ddd.support.domain.protocol.Acknowledged
import ddd.support.domain.command.CommandMessage
import scala.concurrent.duration._

import scala.concurrent.duration.Duration

object AggregateRoot {
  type Event = DomainEvent
}

trait AggregateState {
  type StateMachine = PartialFunction[Event, AggregateState]
  def apply: StateMachine
}

abstract class AggregateRootActorFactory[A <: AggregateRoot[_]] extends BusinessEntityActorFactory[A] {
  def props(passivationConfig: PassivationConfig): Props
  def inactivityTimeout: Duration = 1.minute
}

trait AggregateRoot[S <: AggregateState]
  extends BusinessEntity with GracefulPassivation with PersistentActor with EventHandler with ActorLogging {

  type AggregateRootFactory = PartialFunction[Event, S]
  private var stateOpt: Option[S] = None
  private var _lastCommandMessage: Option[CommandMessage] = None
  val factory: AggregateRootFactory

  override def persistenceId: String = id
  override def id = self.path.name

  override def receiveCommand: Receive = {
    case cm: CommandMessage =>
      _lastCommandMessage = Some(cm)
      handleCommand.applyOrElse(cm.command, unhandled)
  }

  override def receiveRecover: Receive = {
    case event: EventMessage =>
      updateState(event.event)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }

  def commandMessage = _lastCommandMessage.get

  def handleCommand: Receive

  def updateState(event: Event) {
    val nextState = if (initialized) state.apply(event) else factory.apply(event)
    stateOpt = Option(nextState.asInstanceOf[S])
  }

  def raise(event: Event) {
    persist(new EventMessage(event = event, metaData = commandMessage.metaData)) {
      persisted =>
        {
          log.info("Event persisted: {}", event)
          updateState(event)
          handle(toDomainEventMessage(persisted))
        }
    }
  }

  def toDomainEventMessage(persisted: EventMessage) =
    new DomainEventMessage(persisted, SnapshotId(id, lastSequenceNr))

  override def handle(event: DomainEventMessage) {
    sender ! Acknowledged
  }

  def initialized = stateOpt.isDefined

  def state = if (initialized) stateOpt.get else throw new AggregateRootNotInitializedException

}
