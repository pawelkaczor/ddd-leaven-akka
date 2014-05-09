package test.support

import akka.actor.Actor
import akka.persistence.ConfirmablePersistent
import ddd.support.domain.event.DomainEventMessage

class LocalEventPublisher extends Actor {
  override def receive: Receive = {
    case p @ ConfirmablePersistent(DomainEventMessage(_, event, _, _), _, _) =>
      context.system.eventStream.publish(event)
      p.confirm()
  }
}
