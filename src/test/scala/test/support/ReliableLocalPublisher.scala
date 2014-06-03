package test.support

import ddd.support.domain.event.DomainEvent

class ReliableLocalPublisher extends ReliableEventHandler {
  override def handle(event: DomainEvent) = context.system.eventStream.publish(event)
}
