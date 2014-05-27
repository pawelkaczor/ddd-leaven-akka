package ecommerce.system.infrastructure.events

import akka.actor.{ActorRef, Actor}
import akka.camel.{CamelMessage, Producer}
import infrastructure.akka.SerializationSupport
import akka.persistence.{Persistent, ConfirmablePersistent}
import ddd.support.domain.event.EventMessage
import EventMessageConfirmableProducer._
import ddd.support.domain.protocol.Published
import ddd.support.domain.event.EventMessage.ReplyTo

object EventMessageConfirmableProducer {
  val ConfirmableInfo = "ConfirmableInfo"
}

/**
 * Forwards payloads (of type EventMessage) of incoming ConfirmablePersistent messages to defined endpoint.
 * Confirms to sender once event message is delivered to endpoint.
 */
abstract class EventMessageConfirmableProducer extends Actor with Producer with SerializationSupport {

  override def transformOutgoingMessage(msg: Any): Any = msg match {
    case cp:ConfirmablePersistent => unwrapEventMessage(cp)
  }

  override def routeResponse(msg: Any) {
    msg match {
      case CamelMessage(eventMsg:EventMessage, _) =>
        rewrapToConfirmable(eventMsg).confirm()
        if (eventMsg.hasMetaAttribute(ReplyTo)) {
          getReplyTo(eventMsg) ! Published
        }
    }
  }

  def getReplyTo(eventMsg: EventMessage): ActorRef = {
    deserialize[ActorRef](eventMsg.getMetaAttribute(ReplyTo))
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
