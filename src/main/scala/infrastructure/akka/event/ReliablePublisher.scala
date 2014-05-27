package infrastructure.akka.event

import akka.actor.ActorPath
import akka.persistence._
import ddd.support.domain.{SnapshotId, AggregateRoot}
import scala.concurrent.duration._
import ddd.support.domain.event.{DomainEventMessage, DomainEvent, EventPublisher}

trait ReliablePublisher extends EventsourcedProcessor with EventPublisher {
  this: AggregateRoot[_] =>

  def target: ActorPath

  val redeliverInterval = 30.seconds
  val redeliverMax = 15

  val channel = context.actorOf(
    Channel.props(ChannelSettings(redeliverInterval = redeliverInterval, redeliverMax = redeliverMax)),
    name = "publishChannel")


  override def publish(event: DomainEvent) {
    channel ! Deliver(Persistent(DomainEventMessage(SnapshotId(aggregateId, lastSequenceNr), event)), target)
  }

  abstract override def receiveRecover: Receive = {
    case msg: DomainEvent =>
      super.receiveRecover(msg)
      publish(msg)
  }

}
