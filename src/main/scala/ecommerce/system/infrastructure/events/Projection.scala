package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props}
import akka.camel.Ack
import ddd.support.domain.event.DomainEventMessage
import ddd.support.domain.protocol.ViewUpdated

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
    spec.apply(eventMessage)
    sender ! toResponse(eventMessage)
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
