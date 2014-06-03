package infrastructure.actor

import akka.actor.{PoisonPill, ReceiveTimeout, Actor}
import scala.concurrent.duration._

@SerialVersionUID(1L)
case class Passivate(stopMessage: Any)

case class PassivationConfig(passivationMsg: Any = PoisonPill, inactivityTimeout: Duration = 30.minutes)

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
