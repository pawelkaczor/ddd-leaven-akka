package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props}
import akka.camel.Ack
import ddd.support.domain.event.DomainEventMessage

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
    sender ! (if (sendEventAsAck) eventMessage else Ack)
  }

}
