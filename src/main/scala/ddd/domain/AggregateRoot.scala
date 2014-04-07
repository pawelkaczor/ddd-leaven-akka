package ddd.domain

import akka.actor.ActorLogging
import ddd.domain.event.{AggregateRootCreated, DomainEvent}

trait AggregateState {
  def apply(event: DomainEvent): AggregateState
}

trait AggregateRoot[S <: AggregateState] {
  this: ActorLogging =>

  var state: Option[AggregateState] = None

  def apply(event: DomainEvent)(implicit factory: AggregateRootCreated => Option[S]): S = {
    log.info("Applying event: {}", event.getClass.getSimpleName)
    state = event match {
      case AggregateRootCreated(_) =>
        factory.apply(event.asInstanceOf[AggregateRootCreated])
      case _ => Option(state.get.apply(event))
    }
    log.info("Event applied: {}", event.getClass.getSimpleName)
    getState
  }

  def getState = if (created) state.get.asInstanceOf[S] else throw new RuntimeException("Aggregate root has not been yet created.")

  def created = state.isDefined
}
