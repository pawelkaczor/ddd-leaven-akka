package ddd.support.domain

import ddd.support.domain.IdResolution.EntityIdResolver
import ddd.support.domain.command.Command

class AggregateIdResolution[A] extends EntityIdResolution[A] {

  override def entityIdResolver: EntityIdResolver = {
    super.entityIdResolver.orElse {
      case c: Command => c.aggregateId
    }
  }
}

