package ecommerce.businessprocess

import akka.actor.ActorRef
import ddd.support.domain.Saga.SagaState
import ddd.support.domain.Saga
import ecommerce.businessprocess.OrderSaga.{ Accepted, State }
import ecommerce.invoicing.BillingService.{ BillCustomer, CustomerBilled }
import ecommerce.sales.domain.reservation.Reservation.ReservationConfirmed
import ecommerce.sales.integration.OrderTopic
import ecommerce.system.infrastructure.process.SagaSupport.ExchangeSubscriptions
import infrastructure.actor.PassivationConfig

object OrderSaga {
  class State extends SagaState {
    override def apply = {
      case ReservationConfirmed(_, _) => this
      case CustomerBilled(_, _) => Paid
    }
  }

  case object Accepted extends State
  case object Paid extends State
  case object Shipped extends State
  case object Completed extends State

  implicit def exchangeSubscriptions: ExchangeSubscriptions[OrderSaga] = {
    Map(OrderTopic.ExchangeName -> Array(classOf[ReservationConfirmed], classOf[CustomerBilled]))
  }
}

class OrderSaga(billingService: ActorRef, override val passivationConfig: PassivationConfig) extends Saga[State] {

  override def initialState: State = Accepted

  override def receiveEvent: Receive = {
    case e @ ReservationConfirmed(orderId, customerId) => raiseEvent(e) {
      billingService ! BillCustomer(customerId, orderId)
    }
    case e @ CustomerBilled(customerId, orderId) => raiseEvent(e) {
      // publish locally just for testing
      context.system.eventStream.publish(e)

      // call ShipmentService
      // ....
    }
  }

}

