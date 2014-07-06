package test.support

import akka.actor.{ Actor, Props }
import ddd.support.domain.event.{ DomainEvent, DomainEventMessage }
import ecommerce.system.DeliveryContext._
import infrastructure.akka.SerializationSupportForActor

object ReliableEventHandler {

  def props(handler: DomainEvent => Unit) = Props(ReliableEventHandler(handler))

  def apply(handler: DomainEvent => Unit) = new ReliableEventHandler {
    override def handle(event: DomainEvent): Unit = handler(event)
  }
}

abstract class ReliableEventHandler extends Actor with SerializationSupportForActor {

  override def receive: Receive = {
    case em: DomainEventMessage =>
      handle(em.event)
      em.confirmIfRequested()

  }

  def handle(event: DomainEvent)
}
