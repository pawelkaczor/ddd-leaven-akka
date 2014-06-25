package test.support

import akka.actor._
import ddd.support.domain.command.{ CommandMessage, Command }
import ddd.support.domain.{ AggregateRootActorFactory, AggregateIdResolution, AggregateRoot }
import ecommerce.sales.domain.reservation.Reservation
import ecommerce.system.infrastructure.office.OfficeFactory
import infrastructure.actor._
import scala.concurrent.duration._
import ecommerce.inventory.domain.Product
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

object LocalOffice {

  implicit object ProductIdResolution extends AggregateIdResolution[Product]
  implicit object ReservationIdResolution extends AggregateIdResolution[Reservation]

  implicit def localOfficeFactory[T <: AggregateRoot[_]](implicit ct: ClassTag[T], creationSupport: CreationSupport): OfficeFactory[T] = {
    new OfficeFactory[T] {
      override def getOrCreate(caseIdResolution: AggregateIdResolution[T], clerkFactory: AggregateRootActorFactory[T]): ActorRef = {
        creationSupport.getOrCreateChild(Props(new LocalOffice[T]()(ct, caseIdResolution, clerkFactory)), officeName(ct))
      }
    }
  }
}

class LocalOffice[T <: AggregateRoot[_]](inactivityTimeout: Duration = 1.minutes)(
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
      val clerkProps = clerkFactory.props(PassivationConfig(Passivate(PoisonPill), clerkFactory.inactivityTimeout))
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
