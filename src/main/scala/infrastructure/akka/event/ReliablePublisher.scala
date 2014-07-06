package infrastructure.akka.event

import akka.actor._
import akka.persistence.AtLeastOnceDelivery.{ UnconfirmedDelivery, UnconfirmedWarning }
import akka.persistence._
import ddd.support.domain.AggregateRoot
import ddd.support.domain.event.{ DomainEventMessage, EventMessage, EventPublisher }
import ddd.support.domain.protocol.Confirm
import infrastructure.akka.event.ReliablePublisher.Confirmed

import scala.collection.immutable.Seq
import scala.concurrent.duration._

object ReliablePublisher {
  case class Confirmed(deliveryId: Long)
}

trait ReliablePublisher extends PersistentActor with EventPublisher with AtLeastOnceDelivery {
  this: AggregateRoot[_] =>

  implicit def system = context.system

  def target: ActorPath

  override def redeliverInterval = 30.seconds
  override def warnAfterNumberOfUnconfirmedAttempts = 15

  override def publish(em: DomainEventMessage) {
    import ecommerce.system.DeliveryContext.Adjust._
    deliver(target, deliveryId => em.requestConfirmation(deliveryId))
  }

  abstract override def receiveRecover: Receive = {
    case event: EventMessage =>
      super.receiveRecover(event)
      publish(toDomainEventMessage(event))

    case Confirmed(deliveryId) =>
      confirmDelivery(deliveryId)
  }

  abstract override def receiveCommand: Receive = {
    case Confirm(deliveryId) =>
      persist(Confirmed(deliveryId)) {
        _ => confirmDelivery(deliveryId)
      }
    case UnconfirmedWarning(unconfirmedDeliveries) =>
      receiveUnconfirmedDeliveries(unconfirmedDeliveries)
    case c => super.receiveCommand(c)
  }

  def receiveUnconfirmedDeliveries(deliveries: Seq[UnconfirmedDelivery]): Unit = {
    // TODO it should be possible define compensation action that will be triggered from here
    // If compensation applied, unconfirmed deliveries should be confirmed: 
    //unconfirmedDeliveries.foreach(ud => confirmDelivery(ud.deliveryId))
  }
}
