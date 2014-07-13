package ecommerce.system.infrastructure.process

import akka.actor.{ ActorSystem, ActorRef }
import ddd.support.domain.event.DomainEvent
import ddd.support.domain._
import ecommerce.system.infrastructure.events.ForwardingConsumer
import ecommerce.system.infrastructure.office.{ Office, OfficeFactory }
import infrastructure.actor.CreationSupport

object SagaSupport {
  type ExchangeName = String

  type ExchangeSubscriptions[A <: Saga[_]] = Map[ExchangeName, Array[Class[_ <: DomainEvent]]]

  implicit def defaultCaseIdResolution[A <: Saga[_]]() = new EntityIdResolution[A]

  def registerSaga[A <: Saga[_] : ExchangeSubscriptions : OfficeFactory : BusinessEntityActorFactory](implicit system: ActorSystem, creator: CreationSupport, caseIdResolution: IdResolution[A] = new EntityIdResolution[A]): ActorRef = {
    val sagaOffice = Office.office[A]
    registerEventListeners(sagaOffice)
    sagaOffice
  }

  private def registerEventListeners[A <: Saga[_]](sagaOffice: ActorRef)(implicit es: ExchangeSubscriptions[_], creator: CreationSupport) {
    for ((exchangeName, events) <- es) {
      ForwardingConsumer(exchangeName, sagaOffice)
    }
  }
}
