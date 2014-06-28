package ddd.support.domain

import akka.actor.Props
import ddd.support.domain.BusinessEntity.EntityId
import ddd.support.domain.IdResolution.EntityIdResolver
import infrastructure.actor.PassivationConfig

import scala.concurrent.duration.Duration

object BusinessEntity {
  type EntityId = String
}

trait BusinessEntity {
  def id: EntityId
}

object IdResolution {
  type EntityIdResolver = PartialFunction[Any, EntityId]
}

trait IdResolution[A] {
  def entityIdResolver: EntityIdResolver
}

abstract class BusinessEntityActorFactory[A <: BusinessEntity] {
  def props(pc: PassivationConfig): Props
  def inactivityTimeout: Duration
}

