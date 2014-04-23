package infrastructure.actor

import akka.actor.{ReceiveTimeout, Actor}
import scala.concurrent.duration.Duration

@SerialVersionUID(1L)
case class Passivate(stopMessage: Any)

trait GracefulPassivation extends Actor {

  val passivationMsg: Any
  val inactivityTimeout: Duration

  context.setReceiveTimeout(inactivityTimeout)

  override def unhandled(message: Any) {
    message match {
      case ReceiveTimeout => context.parent ! passivationMsg
      case _ => super.unhandled(message)
    }
  }

}
