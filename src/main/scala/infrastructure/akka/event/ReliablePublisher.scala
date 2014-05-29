package infrastructure.akka.event

import akka.actor.ActorPath
import akka.persistence._
import ddd.support.domain.{SnapshotId, AggregateRoot}
import scala.concurrent.duration._
import ddd.support.domain.event.{DomainEventMessage, DomainEvent, EventPublisher}
import ddd.support.domain.protocol.Published

trait ReliablePublisher extends EventsourcedProcessor with EventPublisher {
  this: AggregateRoot[_] =>

  implicit def system = context.system

  def target: ActorPath

  val redeliverInterval = 30.seconds
  val redeliverMax = 15

  val channel = context.actorOf(
    Channel.props(ChannelSettings(redeliverInterval = redeliverInterval, redeliverMax = redeliverMax)),
    name = "publishChannel")

  override def publish(event: DomainEvent) {
    channel ! Deliver(Persistent(toEventMessage(event)), target)
  }

  def toEventMessage(event: DomainEvent): Any = {
    val em = DomainEventMessage(SnapshotId(aggregateId, lastSequenceNr), event)
    import ecommerce.system.DeliveryContext.Adjust._
    if (commandMessage.receiptRequested) {
      em.requestDLR().withReceipt(Published).withReceiptRequester(sender())
    }
    em
  }

  abstract override def receiveRecover: Receive = {
    case msg: DomainEvent =>
      super.receiveRecover(msg)
      publish(msg)
  }

}
