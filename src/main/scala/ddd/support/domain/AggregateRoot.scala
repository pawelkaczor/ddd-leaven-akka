package ddd.support.domain

import akka.actor.ActorLogging
import ddd.support.domain.event.DomainEvent
import akka.persistence.EventsourcedProcessor

trait AggregateState {
  type StateMachine = PartialFunction[DomainEvent, AggregateState]
  def apply: StateMachine
}

trait AggregateRoot[S <: AggregateState] extends EventsourcedProcessor with ActorLogging {

  type AggregateRootFactory = PartialFunction[DomainEvent, S]
  type EventHandler = DomainEvent => Unit
  private var stateOpt: Option[S] = None

  val factory: AggregateRootFactory

  override def receiveRecover: Receive = {
    case evt: DomainEvent => updateState(evt)
  }

  private def updateState(event: DomainEvent) {
    val nextState = if (initialized) state.apply(event) else factory.apply(event)
    stateOpt = Option(nextState.asInstanceOf[S])
  }

  def raise(event: DomainEvent)(implicit handler: EventHandler = publish) {
    persist(event) {
      persistedEvent => {
        log.info("Event persisted: {}", event)
        updateState(persistedEvent)
        handler(persistedEvent)
      }
    }
  }

  def publish(event: DomainEvent) {
    context.system.eventStream.publish(event)
  }

  def initialized = stateOpt.isDefined

  protected def state = if (initialized) stateOpt.get else throw new RuntimeException("Aggregate root does not exist")

}
