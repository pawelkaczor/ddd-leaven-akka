package test.support

import akka.actor.Actor
import akka.persistence.ConfirmablePersistent
import ddd.support.domain.event.DomainEventMessage

class EventLocalConfirmablePublisher extends Actor {
  override def receive: Receive = {
    case p @ ConfirmablePersistent(em:DomainEventMessage, _, _) =>
      context.system.eventStream.publish(em.payload)
      p.confirm()
  }
}
