package ecommerce.system.infrastructure.events

import akka.actor.{ Props, ActorRef }
import infrastructure.actor.CreationSupport

object ForwardingConsumer {
  def apply(exchangeName: String, target: ActorRef)(implicit creator: CreationSupport) = {
    creator.createChild(props(exchangeName, target), EventConsumer.name(exchangeName))
  }

  def props(exchangeName: String, target: ActorRef) = {
    Props(new ForwardingConsumer(exchangeName, target))
  }

}

class ForwardingConsumer(override val endpointUri: String, override val target: ActorRef)
  extends EventConsumer with EventForwarding