package infrastructure.akka.broker

import akka.actor.ActorSystem
import org.apache.activemq.camel.component.ActiveMQComponent._
import akka.camel.CamelExtension
import infrastructure.EcommerceSettings

/**
 * Allows messages to be sent to a JMS Queue or Topic
 * or messages to be consumed from a JMS Queue or Topic
 * using Apache ActiveMQ
 */
trait ActiveMQMessaging {
  val system: ActorSystem
  val settings: EcommerceSettings

  val camel = CamelExtension(system)
  val activeMQComp = activeMQComponent(settings.BrokerUrl)
  activeMQComp.setDeliveryPersistent(false)
  activeMQComp.setRequestTimeout(500)
  //activeMQComp.setAcknowledgementMode(javax.jms.Session.CLIENT_ACKNOWLEDGE)
  camel.context.addComponent("activemq", activeMQComp)

}
