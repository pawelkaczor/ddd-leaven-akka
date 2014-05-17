package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props, Actor}
import akka.camel.{CamelMessage, Consumer}
import ddd.support.domain.event.{DomainEvent, DomainEventMessage}
import ecommerce.sales.infrastructure.inventory.InventoryQueue
import ddd.support.domain.AggregateRoot
import ddd.support.domain.AggregateRoot.Event

object EventMessageListener {

  def apply(endpoint: String)(handler: DomainEventMessage => Unit)(implicit system: ActorSystem) = {

    val endpointActorName = s"${endpoint.split(':').last}Listener"

    system.actorOf(Props(new EventMessageListener {
      override def endpointUri = endpoint
      override def handle(eventMessage: DomainEventMessage) = handler(eventMessage)
      override def handle(aggregateId: String, event: Event) = throw new UnsupportedOperationException
    }), name = endpointActorName)
  }

}

abstract class EventMessageListener extends Actor with Consumer {

  override def receive: Receive = {
    case CamelMessage(em:DomainEventMessage, _) =>
      handle(em)
  }

  def handle(eventMessage: DomainEventMessage) {
    handle(eventMessage.aggregateId, eventMessage.payload)
  }

  def handle(aggregateId: String, event: Event)

}
