package test.support

import ddd.support.domain.event.{DomainEventMessage, EventPublisher}
import akka.actor.Actor

trait LocalPublisher extends EventPublisher {
  this: Actor =>

  override def publish(em: DomainEventMessage) {
    context.system.eventStream.publish(em.event)
  }

}
