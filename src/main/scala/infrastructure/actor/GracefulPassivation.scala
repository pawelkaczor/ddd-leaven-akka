package infrastructure.actor

import akka.actor.{ReceiveTimeout, Actor}
import scala.concurrent.duration.Duration

@SerialVersionUID(1L)
case class Passivate(stopMessage: Any)

case class PassivationConfig(passivationMsg: Any, inactivityTimeout: Duration)

trait GracefulPassivation extends Actor {

  val passivationConfig: PassivationConfig

  override def preStart() {
    context.setReceiveTimeout(passivationConfig.inactivityTimeout)
  }

  override def unhandled(message: Any) {
    message match {
      case ReceiveTimeout => context.parent ! passivationConfig.passivationMsg
      case _ => super.unhandled(message)
    }
  }

}
