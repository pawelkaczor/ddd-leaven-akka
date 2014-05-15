package test.support

import ddd.support.domain.{AggregateRoot, EventPublisher}
import ddd.support.domain.AggregateRoot.Event
import akka.event.EventBus

trait LocalPublisher extends EventPublisher {
  this: AggregateRoot[_] =>

  type TargetType = EventBus
  override val target = context.system.eventStream

  override def publish(event: Event) {
    target.publish(event.asInstanceOf[target.Event])
  }

}
