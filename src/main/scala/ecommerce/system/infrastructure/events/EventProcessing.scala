package ecommerce.system.infrastructure.events

import akka.actor.{ Actor, ActorRef, ActorLogging }
import akka.actor.Status.Failure
import akka.camel.{ Ack, CamelMessage, Consumer }
import ddd.support.domain.event.DomainEventMessage
import ddd.support.domain.protocol.Acknowledged

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
