package test.support

import ddd.support.domain.AggregateRoot.Event
import ddd.support.domain.event.EventPublisher
import akka.actor.Actor

trait LocalPublisher extends EventPublisher {
  this: Actor =>

  override def publish(event: Event) {
    context.system.eventStream.publish(event)
  }

}
