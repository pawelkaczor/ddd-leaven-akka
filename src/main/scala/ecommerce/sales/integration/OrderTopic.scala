package ecommerce.sales.integration

import akka.actor.Props
import akka.camel.Oneway
import ecommerce.system.infrastructure.events.EventMessageConfirmableProducer

object OrderTopic {
  val ExchangeName = "activemq:topic:Order"

  val name = "orderTopic"

  def props = Props(new OrderTopic with Oneway)

}

class OrderTopic extends EventMessageConfirmableProducer {
  override def endpointUri = OrderTopic.ExchangeName
}
