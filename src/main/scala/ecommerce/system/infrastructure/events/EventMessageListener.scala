package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props, Actor}
import akka.camel.{CamelMessage, Consumer}
import ddd.support.domain.event.{DomainEvent, DomainEventMessage}
import ecommerce.sales.infrastructure.inventory.InventoryQueue

object EventMessageListener {

  def apply(endpoint: String)(handler: DomainEventMessage => Unit)(implicit system: ActorSystem) = {

    val endpointActorName = s"${endpoint.split(':').last}Listener"

    system.actorOf(Props(new EventMessageListener {
      override def endpointUri = endpoint
      override def handle(eventMessage: DomainEventMessage) = handler(eventMessage)
    }), name = endpointActorName)
  }

}

abstract class EventMessageListener extends Actor with Consumer {

  override def receive: Receive = {
    case msg @ CamelMessage(em:DomainEventMessage, _) =>
      handle(em)
  }

  def handle(eventMessage: DomainEventMessage)
}
