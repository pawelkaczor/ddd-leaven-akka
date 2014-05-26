package infrastructure.akka.event

import akka.actor.ActorRef
import ddd.support.domain.AggregateRoot
import ddd.support.domain.protocol.{Published, Acknowledged}

trait AcknowledgingPublisher extends ReliablePublisher {
  this: AggregateRoot[_] =>

  abstract override def receiveCommand: Receive = {
    super.receiveCommand.andThen {
      case _ => context.become(awaitingAck(sender()))
    }
  }

  def awaitingAck(sender: ActorRef): Receive = {
    case msg if receiveAck(sender).lift(msg).isDefined => context.unbecome()
  }

  def receiveAck(sender: ActorRef): Receive = {
    case Acknowledged => sender ! Published
  }


}
