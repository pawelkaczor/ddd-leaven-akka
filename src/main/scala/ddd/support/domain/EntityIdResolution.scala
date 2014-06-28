package ddd.support.domain

import ddd.support.domain.IdResolution._

class EntityIdResolution[A] extends IdResolution[A] {

  override def entityIdResolver: EntityIdResolver = {
    case em: EntityMessage => em.entityId
  }
}
