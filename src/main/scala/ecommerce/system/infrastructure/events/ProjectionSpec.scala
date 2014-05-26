package ecommerce.system.infrastructure.events

import ddd.support.domain.event.{DomainEvent, DomainEventMessage}
import akka.actor.ActorSystem
import akka.event.Logging

abstract class ProjectionSpec(implicit system: ActorSystem) extends Function[DomainEventMessage, Unit] {

  protected val log = Logging.getLogger(system, this)

  override def apply(event: DomainEventMessage): Unit = apply(event.aggregateId, event.payload)

  def apply(aggregateId: String, event: DomainEvent)
}
