package ddd.domain

import akka.actor.ActorLogging
import ddd.domain.event.DomainEvent

trait AggregateState[E <: DomainEvent] {
  type StateMachine = PartialFunction[E, AggregateState[E]]
  def apply: StateMachine
}

trait AggregateRoot[E <: DomainEvent, S <: AggregateState[E]] {
  this: ActorLogging =>
  var stateOpt: Option[AggregateState[E]] = None

  type AggregateRootFactory = PartialFunction[E, S]

  def apply(event: E)(implicit factory: AggregateRootFactory): S = {
    stateOpt = if (!created && factory.isDefinedAt(event)) {
      Option(factory.apply(event))
    } else {
      Option(state.apply(event))
    }
    log.info("Event applied: {}", event.getClass.getSimpleName)
    state
  }

  def state = if (created) stateOpt.get.asInstanceOf[S] else throw new RuntimeException("Aggregate root has not been yet created.")

  def created = stateOpt.isDefined
}
