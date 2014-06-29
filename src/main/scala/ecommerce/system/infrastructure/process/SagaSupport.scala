package ecommerce.system.infrastructure.process

import akka.actor.ActorRef
import ddd.support.domain.event.DomainEvent
import ddd.support.domain._
import ecommerce.system.infrastructure.events.ForwardingConsumer
import ecommerce.system.infrastructure.office.{ Office, OfficeFactory }
import infrastructure.actor.CreationSupport

object SagaSupport {
  type ExchangeName = String

  type ExchangeSubscriptions[A <: Saga[_]] = Map[ExchangeName, Array[Class[_ <: DomainEvent]]]

  def registerSaga[A <: Saga[_]](implicit es: ExchangeSubscriptions[A], factory: OfficeFactory[A], caseIdResolution: IdResolution[A] = new EntityIdResolution[A],
    sagaActorFactory: BusinessEntityActorFactory[A], cs: CreationSupport): ActorRef = {
    val sagaOffice = Office.office[A]
    registerEventListeners(sagaOffice, es)
    sagaOffice
  }

  private def registerEventListeners(sagaOffice: ActorRef, es: ExchangeSubscriptions[_])(implicit cs: CreationSupport) {
    for ((exchangeName, events) <- es) {
      ForwardingConsumer(exchangeName, sagaOffice)
    }
  }
}
