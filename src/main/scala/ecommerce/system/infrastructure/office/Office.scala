package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain.{ AggregateRootActorFactory, AggregateIdResolution, AggregateRoot }

object Office {

  def office[T <: AggregateRoot[_]](
    implicit factory: OfficeFactory[T],
    caseIdResolution: AggregateIdResolution[T],
    clerkFactory: AggregateRootActorFactory[T]): ActorRef = {

    factory.getOrCreate(caseIdResolution, clerkFactory)
  }

}
