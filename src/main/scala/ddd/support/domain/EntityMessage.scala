package ddd.support.domain

import ddd.support.domain.BusinessEntity.EntityId

trait EntityMessage {
  def entityId: EntityId
  def payload: Any
}
