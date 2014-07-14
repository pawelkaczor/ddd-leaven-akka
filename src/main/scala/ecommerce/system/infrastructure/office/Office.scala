package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain._

object Office {

  def office[A <: BusinessEntity : BusinessEntityActorFactory : IdResolution : OfficeFactory]: ActorRef = {
    implicitly[OfficeFactory[A]].getOrCreate
  }

}
