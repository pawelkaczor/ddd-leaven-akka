package infrastructure.akka.event

import akka.actor.ActorPath
import akka.persistence._
import ddd.support.domain.{SnapshotId, AggregateRoot}
import scala.concurrent.duration._
import ddd.support.domain.event.{DomainEventMessage, DomainEvent, EventPublisher}
import ddd.support.domain.event.EventMessage._
import infrastructure.akka.SerializationSupport
import ddd.support.domain.protocol.Published

trait ReliablePublisher extends EventsourcedProcessor with EventPublisher with SerializationSupport {
  this: AggregateRoot[_] =>

  def target: ActorPath
  def applicationLevelAck = false

  val redeliverInterval = 30.seconds
  val redeliverMax = 15

  val channel = context.actorOf(
    Channel.props(ChannelSettings(redeliverInterval = redeliverInterval, redeliverMax = redeliverMax)),
    name = "publishChannel")


  override def publish(event: DomainEvent) {
    channel ! Deliver(Persistent(toEventMessage(event)), target)
  }

  def toEventMessage(event: DomainEvent) = {
    val message = DomainEventMessage(SnapshotId(aggregateId, lastSequenceNr), event)
    if (applicationLevelAck)
      message.withMetaData(
        Map(
          ReplyTo -> serialize(sender()),
          ReplyWith -> Published
        )
      )
    else
      message
  }

  abstract override def receiveRecover: Receive = {
    case msg: DomainEvent =>
      super.receiveRecover(msg)
      publish(msg)
  }

}
