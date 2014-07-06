package ecommerce.system.infrastructure.events

import akka.actor.{ Actor, ActorLogging, Status }
import akka.camel.{ CamelMessage, Producer }
import ddd.support.domain.event.EventMessage
import ecommerce.system.DeliveryContext._
import infrastructure.akka.SerializationSupportForActor

/**
 * Forwards payloads (of type EventMessage) of incoming ConfirmablePersistent messages to defined endpoint.
 * Confirms to sender once event message is delivered to endpoint.
 */
abstract class EventMessageConfirmableProducer extends Actor with Producer with SerializationSupportForActor with ActorLogging {

  override def routeResponse(msg: Any) {
    msg match {
      case CamelMessage(em: EventMessage, _) =>
        em.confirmIfRequested()
      case Status.Failure(ex) =>
        log.error("Event delivery to {} failed. Reason: {}", endpointUri, ex.toString)
    }
  }

}
