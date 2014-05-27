package ddd.support.domain.event

import java.util.UUID
import ddd.support.domain.AggregateRoot.Event
import ddd.support.domain.SnapshotId

case class DomainEventMessage(
    snapshotId: SnapshotId,
    override val payload: Event,
    override val identifier: String = UUID.randomUUID().toString,
    override val metaData: Map[String, AnyRef] = Map.empty)
  extends EventMessage(payload, identifier, snapshotId.timestamp, metaData) {

  def aggregateId = snapshotId.aggregateId

  def sequenceNr = snapshotId.sequenceNr

  def withMetaData(newMetaData: Map[String, AnyRef]): DomainEventMessage = {
    copy(metaData = newMetaData)
  }

}