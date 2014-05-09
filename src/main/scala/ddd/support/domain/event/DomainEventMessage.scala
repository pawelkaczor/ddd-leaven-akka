package ddd.support.domain.event

import java.util.{UUID, Date}
import ddd.support.domain.AggregateRoot

case class DomainEventMessage(
    aggregateId: String,
    payload: AggregateRoot.Event,
    identifier: String = UUID.randomUUID().toString,
    timestamp: Date = new Date)
  extends Serializable;
