package ecommerce.system.infrastructure.events

import akka.actor._
import ddd.support.domain.event.DomainEventMessage
import infrastructure.actor.CreationSupport

object EventConsumer {

  def apply(exchangeName: String)(handler: DomainEventMessage => Unit)(implicit parent: CreationSupport): ActorRef = {
    parent.createChild(props(exchangeName, handler), name(exchangeName))
  }

  def name(exchangeName: String): String = {
    s"${exchangeName.split(':').last}Consumer"
  }

  def props(exchangeName: String, handler: DomainEventMessage => Unit) = {
    Props(new EventConsumer with SyncEventProcessing {
      override def endpointUri = exchangeName
      override def handle(eventMessage: DomainEventMessage): Unit = handler.apply(eventMessage)
    })
  }

}

abstract class EventConsumer {
  this: EventProcessing =>
}

