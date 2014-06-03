package infrastructure.akka.event

import akka.actor._
import akka.persistence._
import ddd.support.domain.AggregateRoot
import scala.concurrent.duration._
import ddd.support.domain.event.{EventMessage, EventPublisher, DomainEventMessage}
import ddd.support.domain.protocol.Published
import akka.persistence.RedeliverFailure
import scala.Some
import akka.actor.SupervisorStrategy.Escalate

trait ReliablePublisher extends EventsourcedProcessor with EventPublisher {
  this: AggregateRoot[_] =>

  implicit def system = context.system

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: RedeliveryFailedException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  class RedeliverFailureListener extends Actor with ActorLogging {
    def receive = {
      case RedeliverFailure(messages) =>
        throw new RedeliveryFailedException(messages.head.payload.asInstanceOf[DomainEventMessage])
    }
  }

  def target: ActorPath

  val redeliverInterval = 30.seconds
  val redeliverMax = 15
  var channel: ActorRef = _

  override def preStart() {
    val listener = context.actorOf(Props(new RedeliverFailureListener))
    channel = context.actorOf(
      Channel.props(ChannelSettings(redeliverMax, redeliverInterval, Some(listener))),
      name = "publishChannel")
    super.preStart()
  }

  override def publish(event: DomainEventMessage) {
    import ecommerce.system.DeliveryContext.Adjust._
    if (event.receiptRequested && !recoveryRunning) {
      event.withReceipt(Published).withReceiptRequester(sender())
    }
    channel ! Deliver(Persistent(event), target)
  }

  abstract override def receiveRecover: Receive = {
    case event: EventMessage =>
      super.receiveRecover(event)
      publish(toDomainEventMessage(event))
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    reason match {
      case (RedeliveryFailedException(event)) =>
        // TODO it should be possible define compensation action that will be triggered from here
        // If compensation applied, event should be marked as deleted (thus no redelivery after restarting):
        // deleteMessage(message.get.asInstanceOf[Persistent].sequenceNr, permanent = false)

        // omit AggregateRoot#preRestart
        super[EventsourcedProcessor].preRestart(reason, message)
      case _ =>
        super.preRestart(reason, message)
    }
  }

}
