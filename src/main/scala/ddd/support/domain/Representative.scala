package ddd.support.domain

import akka.actor._
import infrastructure.actor.{Passivate, ActorContextCreationSupport}
import scala.reflect.ClassTag
import akka.contrib.pattern.ClusterSharding
import infrastructure.cluster.Shardable
import scala.concurrent.duration._

object Representative {

  def office[T <: AggregateRoot[_]](implicit classTag: ClassTag[T], addressable: Addressable[T],
    system: ActorRefFactory): ActorRef = {
    office[T]()
  }

  def office[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minute)(implicit classTag: ClassTag[T], addressable: Addressable[T],
                        system: ActorRefFactory): ActorRef = {
    system.actorOf(Props(new Office[T](inactivityTimeout)), name = addressable.domain)
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
class Office[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minutes)
    (implicit classTag: ClassTag[T], addressable: Addressable[T])
  extends Representative[T] with ActorContextCreationSupport with Actor with ActorLogging {

  override val _addressable = addressable
  
  def receive: Receive = {
    // TODO (passivation) in-between receiving Passivate and Terminated the office should buffer all incoming messages
    // for the clerk being passivated, when receiving Terminated it should flush the buffer
    case Passivate(stopMessage) =>
      passivate(sender(), stopMessage)
    case msg =>
      val arProps: Props = Props(classTag.runtimeClass.asInstanceOf[Class[T]], Passivate(PoisonPill), inactivityTimeout)
      val clerk = getOrCreateChild(arProps, address(msg))
      clerk forward msg
  }

  def passivate(clerk: ActorRef, stopMessage: Any) {
    log.info(s"Passivating $sender")
    clerk ! stopMessage
  }
}

trait Representative[A] {
  this: Actor =>
  
  val _addressable: Addressable[A]

  def address(msg: Any) = _addressable.addressResolver(msg)
}
