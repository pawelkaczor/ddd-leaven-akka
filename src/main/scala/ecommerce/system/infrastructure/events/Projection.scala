package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props}
import akka.camel.Ack
import ddd.support.domain.event.DomainEventMessage
import ddd.support.domain.protocol.ViewUpdated
import akka.actor.Status.Failure

object Projection {

  def apply(endpoint: String, spec: ProjectionSpec, sendEventAsAck: Boolean = true)
           (implicit system: ActorSystem) = {

    val projectionName = s"${endpoint.split(':').last}Projection"

    system.actorOf(Props(new Projection(spec, sendEventAsAck) {
      override def endpointUri = endpoint
    }), name = projectionName)
  }

}

abstract class Projection(spec: ProjectionSpec, sendEventAsAck: Boolean = true) extends EventListener {

  override def autoAck = false

  override def handle(eventMessage: DomainEventMessage) {
    try {
      spec.apply(eventMessage)
      sender ! toResponse(eventMessage)
    } catch {
      case ex: Exception =>
        log.error("Projection of event {} failed. Reason: {}", eventMessage, ex.toString)
        sender() ! Failure(ex)
    }
  }

  def toResponse(eventMessage: DomainEventMessage): Any =
    if (sendEventAsAck) {
      implicit def system = context.system
      import ecommerce.system.DeliveryContext.Adjust._
      eventMessage.withReceipt(ViewUpdated)
    } else {
      Ack
    }

}
