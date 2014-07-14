package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain._

import scala.reflect.ClassTag

abstract class OfficeFactory[A <: BusinessEntity : BusinessEntityActorFactory : IdResolution : ClassTag] {

  def getOrCreate: ActorRef

  def officeName = implicitly[ClassTag[A]].runtimeClass.getSimpleName
}
