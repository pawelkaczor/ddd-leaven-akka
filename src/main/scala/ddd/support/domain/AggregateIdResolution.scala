package ddd.support.domain

import ddd.support.domain.AggregateIdResolution.AggregateIdResolver

object AggregateIdResolution {
  type Command = Any
  type AggregateId = String
  type AggregateIdResolver = PartialFunction[Command, AggregateId]
}

trait AggregateIdResolution[T] {
  def aggregateIdResolver: AggregateIdResolver
}



