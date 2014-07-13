package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain._

import scala.reflect.ClassTag

abstract class OfficeFactory[A <: BusinessEntity : BusinessEntityActorFactory : ClassTag] {

  def getOrCreate(implicit caseIdResolution: IdResolution[A]): ActorRef

  def officeName = implicitly[ClassTag[A]].runtimeClass.getSimpleName
}
