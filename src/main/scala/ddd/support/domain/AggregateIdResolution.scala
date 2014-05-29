package ddd.support.domain

import ddd.support.domain.command.{CommandMessage, Command}
import ddd.support.domain.AggregateIdResolution.AggregateIdResolver

object AggregateIdResolution {
  type AggregateId = String
  type AggregateIdResolver = PartialFunction[Any, AggregateId]
}

class AggregateIdResolution[T] {
  def aggregateIdResolver: AggregateIdResolver = {
    case c: Command => c.aggregateId
    case cm: CommandMessage => cm.command.aggregateId
  }
}



