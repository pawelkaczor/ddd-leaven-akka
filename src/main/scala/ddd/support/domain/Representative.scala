package ddd.support.domain

import akka.actor._
import infrastructure.actor.{Passivate, ActorContextCreationSupport}
import scala.reflect.ClassTag
import akka.contrib.pattern.ClusterSharding
import infrastructure.cluster.ShardResolution
import scala.concurrent.duration._

object Office {

  def office[T <: AggregateRoot[_]](implicit classTag: ClassTag[T], caseIdResolution: AggregateIdResolution[T],
    system: ActorRefFactory): ActorRef = {
    office[T]()
  }

  def office[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minute)
      (implicit classTag: ClassTag[T], caseIdResolution: AggregateIdResolution[T], system: ActorRefFactory): ActorRef = {
    system.actorOf(Props(new Office[T](inactivityTimeout)), name = caseIdResolution.domain)
  }

  def globalOffice[T](implicit classTag: ClassTag[T], shardResolution: ShardResolution[T], system: ActorSystem): ActorRef = {
    ClusterSharding(system).shardRegion(shardResolution.domain)
  }

}

class Office[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minutes)
    (implicit classTag: ClassTag[T], caseIdResolution: AggregateIdResolution[T])
  extends ActorContextCreationSupport with Actor with ActorLogging {

  def receive: Receive = {
    // TODO (passivation) in-between receiving Passivate and Terminated the office should buffer all incoming messages
    // for the clerk being passivated, when receiving Terminated it should flush the buffer
    case Passivate(stopMessage) =>
      dismiss(sender(), stopMessage)
    case msg =>
      val caseProps = Props(classTag.runtimeClass.asInstanceOf[Class[T]], Passivate(PoisonPill), inactivityTimeout)
      val clerk = assignClerk(caseProps, resolveCaseId(msg))
      clerk forward msg
  }

  def resolveCaseId(msg: Any) = caseIdResolution.aggregateIdResolver(msg)

  def assignClerk(caseProps: Props, caseId: String): ActorRef = getOrCreateChild(caseProps, caseId)
  
  def dismiss(clerk: ActorRef, stopMessage: Any) {
    log.info(s"Passivating $sender")
    clerk ! stopMessage
  }
}

