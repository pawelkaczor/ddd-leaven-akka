package test.support

import akka.actor.{Props, Actor}
import akka.persistence.ConfirmablePersistent
import ddd.support.domain.event.{DomainEvent, DomainEventMessage}

object ReliableEventHandler {

  def props(handler: DomainEvent => Unit) = Props(ReliableEventHandler(handler))

  def apply(handler: DomainEvent => Unit) = new ReliableEventHandler {
    override def handle(event: DomainEvent): Unit = handler(event)
  }
}

abstract class ReliableEventHandler extends Actor {

  override def receive: Receive = {
    case p @ ConfirmablePersistent(em:DomainEventMessage, _, _) =>
      handle(em.event)
      p.confirm()
  }

  def handle(event: DomainEvent)
}
