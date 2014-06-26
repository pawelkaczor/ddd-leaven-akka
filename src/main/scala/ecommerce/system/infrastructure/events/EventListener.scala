package ecommerce.system.infrastructure.events

import akka.actor.Status.Failure
import akka.actor._
import akka.camel.{ Ack, CamelMessage, Consumer }
import ddd.support.domain.event.DomainEventMessage
import ddd.support.domain.protocol.Acknowledged
import ecommerce.system.infrastructure.events.EventListener.name
import infrastructure.actor.CreationSupport

object EventListener {

  def apply(exchangeName: String)(handler: DomainEventMessage => Unit)(implicit parent: CreationSupport): ActorRef = {
    parent.createChild(props(exchangeName, handler), name(exchangeName))
  }

  def name(exchangeName: String): String = {
    s"${exchangeName.split(':').last}Consumer"
  }

  def props(exchangeName: String, handler: DomainEventMessage => Unit) = {
    Props(new EventListener with SyncEventProcessing {
      override def endpointUri = exchangeName
      override def handle(eventMessage: DomainEventMessage): Unit = handler.apply(eventMessage)
    })
  }

}

trait EventProcessing extends Consumer with ActorLogging {

  override def autoAck = false

  override def receive: Receive = {
    case CamelMessage(em: DomainEventMessage, _) =>
      process(em)
  }

  def acknowledge(em: DomainEventMessage): Unit = {
    sender ! Ack
  }

  def process(em: DomainEventMessage)

}

trait SyncEventProcessing extends EventProcessing {

  def handle(em: DomainEventMessage)

  override def process(em: DomainEventMessage): Unit = {
    try {
      handle(em)
      acknowledge(em)
    } catch {
      case ex: Exception =>
        log.error("Processing of event {} failed. Reason: {}", em, ex.toString)
        sender ! Failure(ex)
    }
  }

}

trait EventForwarding extends EventProcessing {

  def target: ActorRef

  def waitingForAck(em: DomainEventMessage): Actor.Receive = {
    case Acknowledged(msg) if msg == em =>
      acknowledge(em)
      context.unbecome()
  }

  override def process(em: DomainEventMessage): Unit = {
    target ! em
    context.become(waitingForAck(em))
  }

}

abstract class EventListener {
  this: EventProcessing =>

}

object ForwardingConsumer {
  def apply(exchangeName: String, target: ActorRef)(implicit creator: CreationSupport) = {
    creator.createChild(props(exchangeName, target), name(exchangeName))
  }

  def props(exchangeName: String, target: ActorRef) = {
    Props(new ForwardingConsumer(exchangeName, target))
  }

}

class ForwardingConsumer(override val endpointUri: String, override val target: ActorRef) extends EventListener with EventForwarding