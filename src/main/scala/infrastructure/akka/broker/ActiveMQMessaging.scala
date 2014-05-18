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

  val camel = CamelExtension(system)
  val activeMQComp = activeMQComponent(EcommerceSettings(system).BrokerUrl)

  camel.context.addComponent("activemq", activeMQComp)

}
