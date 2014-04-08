package ddd.domain

import akka.actor.ActorLogging
import ddd.domain.event.DomainEvent

trait AggregateState {
  type StateMachine = PartialFunction[DomainEvent, AggregateState]
  def apply: StateMachine
}

trait AggregateRoot[S <: AggregateState] {
  this: ActorLogging =>

  type AggregateRootFactory = PartialFunction[DomainEvent, S]
  private var stateOpt: Option[S] = None

  def apply(event: DomainEvent)(implicit factory: AggregateRootFactory): S = {
    val nextState = if (!created && factory.isDefinedAt(event))
      factory.apply(event)
    else
      state.apply(event)
    log.info("Event applied: {}", event)
    stateOpt = Option(nextState.asInstanceOf[S])
    state
  }

  def state = if (created) stateOpt.get else throw new RuntimeException("Aggregate root does not exist.")

  def created = stateOpt.isDefined
}
