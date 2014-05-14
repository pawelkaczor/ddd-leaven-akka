package ddd.support.domain.event

import java.util.{UUID, Date}
import ddd.support.domain.AggregateRoot
import ddd.support.domain.AggregateRoot.Event

case class DomainEventMessage(
    aggregateId: String,
    override val payload: Event,
    override val identifier: String = UUID.randomUUID().toString,
    override val timestamp: Date = new Date,
    override val metaData: Map[String, AnyRef] = Map.empty)
  extends EventMessage(payload, identifier, timestamp, metaData) {

  def withMetaData(newMetaData: Map[String, AnyRef]): DomainEventMessage = {
    copy(metaData = newMetaData)
  }

}