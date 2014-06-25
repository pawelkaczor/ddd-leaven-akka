package ecommerce.system.infrastructure.office

import akka.actor.ActorRef
import ddd.support.domain.{AggregateRootActorFactory, AggregateIdResolution, AggregateRoot}

import scala.reflect.ClassTag

trait OfficeFactory[T <: AggregateRoot[_]] {
  def getOrCreate(caseIdResolution: AggregateIdResolution[T], clerkFactory: AggregateRootActorFactory[T]): ActorRef
  def officeName(classTag: ClassTag[T]) = classTag.runtimeClass.getSimpleName
}
