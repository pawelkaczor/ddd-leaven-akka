package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props, Actor}
import akka.camel.{CamelMessage, Consumer}
import ddd.support.domain.event.DomainEventMessage

object EventListener {

  def apply(endpoint: String)(handler: DomainEventMessage => Unit)(implicit system: ActorSystem) = {

    val listenerName = s"${endpoint.split(':').last}Listener"

    system.actorOf(Props(new EventListener {
      override def endpointUri = endpoint
      override def handle(eventMessage: DomainEventMessage): Unit = handler.apply(eventMessage)
      
    }), name = listenerName)
  }

}

abstract class EventListener extends Actor with Consumer {

  override def receive: Receive = {
    case CamelMessage(em:DomainEventMessage, _) =>
      handle(em)
  }

  def handle(eventMessage: DomainEventMessage)

}
