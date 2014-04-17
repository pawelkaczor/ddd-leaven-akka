package ddd.support.domain

import akka.actor._
import infrastructure.actor.ActorContextCreationSupport
import scala.reflect.ClassTag

object Representative {
  def representative[T](implicit classTag: ClassTag[T], addressable: Addressable[T],
                        system: ActorRefFactory): ActorRef = {
    system.actorOf(Props(new Office[T]), name = addressable.domain)
  }

  // more friendly name
  def office[T](implicit classTag: ClassTag[T], addressable: Addressable[T],
                        system: ActorRefFactory): ActorRef = {
    representative[T]
  }
}

/**
 * Default representative of any Addressable.
 * Incoming message is simply forwarded to its addressee.
 */
class Office[T](implicit classTag: ClassTag[T], addressable: Addressable[T])
  extends Representative[T] with ActorContextCreationSupport with Actor {

  override val _addressable = addressable
  
  def receive: Receive = {
    case msg =>
      val target = getOrCreateChild(Props(classTag.runtimeClass.asInstanceOf[Class[T]]), address(msg))
      deliver(target, msg)
  }


  def deliver(target: ActorRef, msg: Any) {
    target forward msg
  }
}

trait Representative[A] {
  this: Actor =>
  
  val _addressable: Addressable[A]

  def address(msg: Any) = _addressable.getAddress(msg)

}
