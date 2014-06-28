package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain._

object Office {

  def office[A <: BusinessEntity](
    implicit factory: OfficeFactory[A],
    caseIdResolution: IdResolution[A] = new AggregateIdResolution[A],
    clerkFactory: BusinessEntityActorFactory[A]): ActorRef = {

    factory.getOrCreate(caseIdResolution, clerkFactory)
  }

}
