package test.support

import akka.actor._
import ddd.support.domain._
import ddd.support.domain.command.{ Command, CommandMessage }
import ecommerce.system.infrastructure.office.OfficeFactory
import infrastructure.actor._

import scala.concurrent.duration._
import scala.reflect.ClassTag

object LocalOffice {

  implicit def localOfficeFactory[A <: BusinessEntity: BusinessEntityActorFactory: IdResolution : ClassTag](implicit system: ActorSystem): OfficeFactory[A] = {
    new OfficeFactory[A] {
      override def getOrCreate: ActorRef = {
        system.actorOf(Props(new LocalOffice[A]()), officeName)
      }
    }
  }
}

class LocalOffice[A <: BusinessEntity](inactivityTimeout: Duration = 1.minutes)(
  implicit ct: ClassTag[A],
  caseIdResolution: IdResolution[A],
  clerkFactory: BusinessEntityActorFactory[A])
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
    case msg: EntityMessage =>
      val clerkProps = clerkFactory.props(PassivationConfig(Passivate(PoisonPill), clerkFactory.inactivityTimeout))
      val clerk = assignClerk(clerkProps, resolveCaseId(msg))
      clerk forward msg
  }

  def resolveCaseId(msg: Any) = caseIdResolution.entityIdResolver(msg)

  def assignClerk(caseProps: Props, caseId: String): ActorRef = getOrCreateChild(caseProps, caseId)

  def dismiss(clerk: ActorRef, stopMessage: Any) {
    log.info(s"Passivating $sender")
    clerk ! stopMessage
  }
}
