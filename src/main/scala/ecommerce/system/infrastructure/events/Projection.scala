package ecommerce.system.infrastructure.events

import akka.actor.{ ActorRef, ActorSystem, Props }
import ddd.support.domain.event.DomainEventMessage
import ddd.support.domain.protocol.ViewUpdated
import infrastructure.actor.CreationSupport

object Projection {

  def apply(exchangeName: String, spec: ProjectionSpec)(implicit creator: CreationSupport): ActorRef = {
    creator.createChild(props(exchangeName, spec), name(exchangeName))
  }

  def props(exchangeName: String, spec: ProjectionSpec): Props = {
    Props(new Projection(spec) {
      override def endpointUri = exchangeName
    })
  }

  def name(exchangeName: String): String = {
    s"${exchangeName.split(':').last}Projection"
  }
}

abstract class Projection(spec: ProjectionSpec) extends EventListener with SyncEventProcessing {

  override def handle(em: DomainEventMessage) {
    spec.apply(em)
  }

  override def acknowledge(em: DomainEventMessage): Unit = {
    super.acknowledge(em)

    implicit def system = context.system
    import ecommerce.system.DeliveryContext._

    em.sendReceiptIfRequested(ViewUpdated(em.event))
  }
}
