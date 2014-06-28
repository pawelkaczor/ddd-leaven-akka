package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain._

import scala.reflect.ClassTag

trait OfficeFactory[A <: BusinessEntity] {
  def getOrCreate(caseIdResolution: IdResolution[A], clerkFactory: BusinessEntityActorFactory[A]): ActorRef
  def officeName(classTag: ClassTag[A]) = classTag.runtimeClass.getSimpleName
}
