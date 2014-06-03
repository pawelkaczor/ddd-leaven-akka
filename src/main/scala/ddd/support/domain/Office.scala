package ddd.support.domain

import akka.actor._
import infrastructure.actor.{PassivationConfig, Passivate, ActorContextCreationSupport}
import scala.reflect.ClassTag
import akka.contrib.pattern.ClusterSharding
import scala.concurrent.duration._
import infrastructure.actor.Passivate
import infrastructure.actor.PassivationConfig
import ddd.support.domain.command.{Command, CommandMessage}

object Office {

  def office[T <: AggregateRoot[_]](
      implicit classTag: ClassTag[T],
      caseIdResolution: AggregateIdResolution[T],
      clerkFactory: AggregateRootActorFactory[T],
      system: ActorRefFactory): ActorRef = {

    office[T]()
  }

  def office[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minute)(
      implicit classTag: ClassTag[T],
      caseIdResolution: AggregateIdResolution[T],
      clerkFactory: AggregateRootActorFactory[T],
      system: ActorRefFactory): ActorRef = {

    system.actorOf(Props(new Office[T](inactivityTimeout)), officeName(classTag))
  }

  def globalOffice[T](implicit classTag: ClassTag[T], system: ActorSystem): ActorRef = {
    ClusterSharding(system).shardRegion(officeName(classTag))
  }

  def officeName[T](classTag: ClassTag[T]) = classTag.runtimeClass.getSimpleName
}

class Office[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minutes)(
    implicit arClassTag: ClassTag[T],
    caseIdResolution: AggregateIdResolution[T],
    clerkFactory: AggregateRootActorFactory[T])
  extends ActorContextCreationSupport with Actor with ActorLogging {

  override def aroundReceive(receive: Actor.Receive, msg: Any): Unit = {
    receive.applyOrElse(msg match {
      case c: Command => CommandMessage(c)
      case other => other
    }, unhandled)
  }

  def receive: Receive = {
    // TODO (passivation) in-between receiving Passivate and Terminated the office should buffer all incoming messages
    // for the clerk being passivated, when receiving Terminated it should flush the buffer
    case Passivate(stopMessage) =>
      dismiss(sender(), stopMessage)
    case cm: CommandMessage =>
      val clerkProps = clerkFactory.props(PassivationConfig(Passivate(PoisonPill), inactivityTimeout))
      val clerk = assignClerk(clerkProps, resolveCaseId(cm.command))
      clerk forward cm
  }

  def resolveCaseId(msg: Command) = caseIdResolution.aggregateIdResolver(msg)

  def assignClerk(caseProps: Props, caseId: String): ActorRef = getOrCreateChild(caseProps, caseId)
  
  def dismiss(clerk: ActorRef, stopMessage: Any) {
    log.info(s"Passivating $sender")
    clerk ! stopMessage
  }
}

