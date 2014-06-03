package ecommerce.system.infrastructure.events

import akka.actor.{ActorSystem, Props}
import akka.camel.Ack
import ddd.support.domain.event.DomainEventMessage
import ddd.support.domain.protocol.ViewUpdated
import akka.actor.Status.Failure

object Projection {
  def apply(endpoint: String, spec: ProjectionSpec)(implicit system: ActorSystem) = {
    system.actorOf(Props(new Projection(spec) {
      override def endpointUri = endpoint
    }), name = s"${endpoint.split(':').last}Projection")
  }
}

abstract class Projection(spec: ProjectionSpec) extends EventListener {

  override def autoAck = false

  implicit def system = context.system
  import ecommerce.system.DeliveryContext._

  override def handle(em: DomainEventMessage) {
    try {
      spec.apply(em)
      sender ! Ack
      em.sendReceiptIfRequested(ViewUpdated(em.event))
    } catch {
      case ex: Exception =>
        log.error("Projection of event {} failed. Reason: {}", em, ex.toString)
        sender() ! Failure(ex)
    }
  }

}
