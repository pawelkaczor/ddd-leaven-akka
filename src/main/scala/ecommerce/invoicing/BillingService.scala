package ecommerce.invoicing

import akka.actor.{ ActorLogging, Actor, ActorRef, Props }
import akka.persistence._
import ddd.support.domain.BusinessEntity.EntityId
import ddd.support.domain.SnapshotId
import ddd.support.domain.command.Command
import ddd.support.domain.event.{ EventMessage, DomainEventMessage, DomainEvent }
import ecommerce.invoicing.BillingService.{ BillCustomer, CustomerBilled }
import infrastructure.actor.CreationSupport

object BillingService {
  def apply(orderTopic: ActorRef)(implicit parent: CreationSupport): ActorRef = {
    parent.createChild(props(orderTopic), "BillingService")
  }

  def props(orderTopic: ActorRef) = Props(new BillingService(orderTopic))

  // commands 
  case class BillCustomer(customerId: EntityId, orderId: EntityId) extends Command {
    override def aggregateId: String = orderId
  }

  // event
  case class CustomerBilled(customerId: EntityId, orderId: String) extends DomainEvent
}

// Dummy implementation 
class BillingService(orderTopic: ActorRef) extends Actor with ActorLogging {

  lazy val channel = context.actorOf(PersistentChannel.props(), name = "publishChannel")

  override def receive: Receive = {
    case BillCustomer(customerId, orderId) =>
      billCustomer(customerId, orderId)
      publish(orderId, CustomerBilled(customerId, orderId))

  }

  def publish(entityId: EntityId, event: DomainEvent): Unit = {
    channel ! Deliver(Persistent(toEventMessage(entityId, event)), orderTopic.path)
  }

  def toEventMessage(entityId: EntityId, event: DomainEvent) = {
    val eventMsg = new EventMessage(event)
    new DomainEventMessage(eventMsg, new SnapshotId(entityId, 1))
  }

  def billCustomer(customerId: EntityId, orderId: EntityId) = {
    import pl.project13.scala.rainbow.Rainbow._
    println(s"Customer $customerId billed for order $orderId".yellow)
  }

}
