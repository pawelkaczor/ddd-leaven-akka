package ddd.support.domain

import akka.actor.{ Props, ActorRef, ActorLogging }
import akka.persistence.{ PersistentActor, EventsourcedProcessor }
import ddd.support.domain.Saga.SagaState
import ddd.support.domain.event.{ DomainEvent, EventMessage }
import ddd.support.domain.protocol.Acknowledged
import infrastructure.actor.{ PassivationConfig, GracefulPassivation }

object Saga {
  trait SagaState {
    type StateMachine = PartialFunction[DomainEvent, SagaState]

    def apply: StateMachine
  }
}

trait Saga[S <: SagaState] extends BusinessEntity
  with GracefulPassivation with PersistentActor with ActorLogging {

  def initialState: S

  val state = initialState

  def sagaId = self.path.name

  override def id = sagaId

  private var _lastEventMessage: Option[EventMessage] = None

  def receiveEvent: Receive

  override def persistenceId: String = sagaId

  override def receiveCommand: Receive = {
    case em: EventMessage =>
      _lastEventMessage = Some(em)
      receiveEvent.applyOrElse(em.event, unhandledEvent)
  }

  def unhandledEvent(em: DomainEvent): Unit = {
    acknowledge(sender())
  }

  override def receiveRecover: Receive = {
    case em: EventMessage =>
      state.apply(em.event)
  }

  def raiseEvent(event: DomainEvent)(action: => Unit) {
    val eventSender = sender()
    persist(new EventMessage(event)) {
      acknowledge(eventSender)
      persisted =>
        {
          state.apply(event)
          action
        }
    }
  }

  def acknowledge(sender: ActorRef) {
    sender ! Acknowledged(_lastEventMessage.get)
  }
}

abstract class SagaActorFactory[A <: Saga[_]] extends BusinessEntityActorFactory[A] {
  import scala.concurrent.duration._

  def props(passivationConfig: PassivationConfig): Props
  def inactivityTimeout: Duration = 1.minute
}
