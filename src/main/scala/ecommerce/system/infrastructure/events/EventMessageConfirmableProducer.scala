package ecommerce.system.infrastructure.events

import akka.actor.{ActorLogging, Status, Actor}
import akka.camel.{CamelMessage, Producer}
import infrastructure.akka.SerializationSupportForActor
import akka.persistence.{Persistent, ConfirmablePersistent}
import ddd.support.domain.event.EventMessage
import EventMessageConfirmableProducer._
import ecommerce.system.DeliveryContext._

object EventMessageConfirmableProducer {
  val ConfirmableInfo = "ConfirmableInfo"
}

/**
 * Forwards payloads (of type EventMessage) of incoming ConfirmablePersistent messages to defined endpoint.
 * Confirms to sender once event message is delivered to endpoint.
 */
abstract class EventMessageConfirmableProducer extends Actor with Producer with SerializationSupportForActor with ActorLogging {

  override def transformOutgoingMessage(msg: Any): Any = msg match {
    case cp:ConfirmablePersistent => unwrapEventMessage(cp)
  }

  override def routeResponse(msg: Any) {
    msg match {
      case CamelMessage(eventMsg:EventMessage, _) =>
        rewrapToConfirmable(eventMsg).confirm()
        if (eventMsg.receiptRequested) {
          eventMsg.receiptRequester ! eventMsg.receipt
        }
      case Status.Failure(ex) =>
        log.error("Event delivery to {} failed. Reason: {}", endpointUri, ex.toString)
    }
  }

  def unwrapEventMessage(cp: ConfirmablePersistent) = cp match {
    case ConfirmablePersistent(eventMsg:EventMessage, _, _) =>
      eventMsg.withMetaData(Map(ConfirmableInfo -> serialize(cp.withPayload(null))))
  }

  def rewrapToConfirmable(eventMsg: EventMessage): ConfirmablePersistent = {
    val sourceEventSerialized = eventMsg.metaData.get(ConfirmableInfo).get.asInstanceOf[Array[Byte]]
    val cp = deserialize[ConfirmablePersistent](sourceEventSerialized, Option(Persistent().getClass))
    cp.withPayload(eventMsg).asInstanceOf[ConfirmablePersistent]
  }

}
