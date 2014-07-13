package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain._

object Office {

  def office[A <: BusinessEntity : BusinessEntityActorFactory : OfficeFactory](
    implicit caseIdResolution: IdResolution[A] = new AggregateIdResolution[A]): ActorRef = {

    implicitly[OfficeFactory[A]].getOrCreate(caseIdResolution)
  }

}
