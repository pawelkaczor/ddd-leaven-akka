package ecommerce.system.infrastructure.events

import akka.actor.Actor
import akka.camel.{CamelMessage, Consumer}
import ddd.support.domain.event.DomainEventMessage

abstract class EventMessageListener extends Actor with Consumer {

  override def receive: Receive = {
    case msg @ CamelMessage(em:DomainEventMessage, _) =>
      handle(em)
  }

  def handle(eventMessage: DomainEventMessage) {
    context.system.eventStream.publish(eventMessage.payload)
  }
}
