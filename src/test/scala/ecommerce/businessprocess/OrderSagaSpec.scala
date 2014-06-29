package ecommerce.businessprocess

import akka.actor.{ ActorRef, ActorPath, Props }
import ddd.support.domain.{ SagaActorFactory, BusinessEntityActorFactory, AggregateRootActorFactory }
import ddd.support.domain.protocol.Acknowledged
import ecommerce.inventory.domain.Product
import ecommerce.invoicing.BillingService
import ecommerce.invoicing.BillingService.CustomerBilled
import ecommerce.sales.domain.reservation.Reservation
import ecommerce.sales.domain.reservation.Reservation.{ ConfirmReservation, ReserveProduct, CreateReservation }
import ecommerce.sales.integration.OrderTopic
import ecommerce.system.infrastructure.process.SagaSupport.registerSaga
import infrastructure.actor.PassivationConfig
import infrastructure.akka.event.ReliablePublisher
import test.support.EventsourcedAggregateRootSpec
import test.support.TestConfig._
import test.support.broker.EmbeddedBrokerTestSupport
import ecommerce.system.infrastructure.office.Office._
import test.support.LocalOffice._
import OrderSaga._

class OrderSagaSpec extends EventsourcedAggregateRootSpec[Product](testSystem)
  with EmbeddedBrokerTestSupport {

  "Long running order process" should {
    "be started after reservation is confirmed" in {

      // given
      val orderTopic = system.actorOf(OrderTopic.props, OrderTopic.name)
      val billingService = BillingService(orderTopic)

      implicit object ReservationActorFactory extends AggregateRootActorFactory[Reservation] {
        override def props(config: PassivationConfig) = Props(new Reservation(config) with ReliablePublisher {
          override def target: ActorPath = orderTopic.path
        })
      }

      implicit object OrderSagaActorFactory extends SagaActorFactory[OrderSaga] {
        override def props(passivationConfig: PassivationConfig): Props = {
          Props(new OrderSaga(billingService, passivationConfig))
        }
      }

      registerSaga[OrderSaga]
      val reservationOffice = office[Reservation]

      // when
      reservationOffice ! CreateReservation("reservation-1", "client-1")
      expectReply(Acknowledged)

      reservationOffice ! ReserveProduct("reservation-1", "product1", 1)
      expectReply(Acknowledged)

      reservationOffice ! ConfirmReservation("reservation-1")
      expectReply(Acknowledged)

      // then
      expectEventPublished[CustomerBilled]
    }
  }

}
