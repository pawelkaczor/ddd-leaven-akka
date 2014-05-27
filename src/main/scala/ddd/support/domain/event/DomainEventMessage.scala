package ddd.support.domain.event

import java.util.UUID
import ddd.support.domain.AggregateRoot.Event
import ddd.support.domain.SnapshotId

case class DomainEventMessage(
    snapshotId: SnapshotId,
    override val payload: Event,
    override val identifier: String = UUID.randomUUID().toString,
    override val metaData: Map[String, Any] = Map.empty)
  extends EventMessage(payload, identifier, snapshotId.timestamp, metaData) {

  def aggregateId = snapshotId.aggregateId

  def sequenceNr = snapshotId.sequenceNr

  def withMetaData(newMetaData: Map[String, Any], clearExisting: Boolean = false): DomainEventMessage = {
    if (clearExisting) {
      copy(metaData = newMetaData)
    } else {
      copy(metaData = metaData ++ newMetaData)
    }
  }

}