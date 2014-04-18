package ddd.support.domain

import akka.actor._
import infrastructure.actor.ActorContextCreationSupport
import scala.reflect.ClassTag
import ecommerce.sales.domain.reservation.Reservation
import akka.contrib.pattern.ClusterSharding
import infrastructure.cluster.Shardable

object Representative {

  def office[T](implicit classTag: ClassTag[T], addressable: Addressable[T],
                        system: ActorRefFactory): ActorRef = {
    system.actorOf(Props(new Office[T]), name = addressable.domain)
  }

  def globalOffice[T](implicit classTag: ClassTag[T], addressable: Shardable[T],
                system: ActorSystem): ActorRef = {
    ClusterSharding(system).shardRegion(addressable.domain)
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

  def address(msg: Any) = _addressable.addressResolver(msg)
}
