package ddd.support.domain.event

import java.util.UUID
import ddd.support.domain.SnapshotId

case class DomainEventMessage(
    snapshotId: SnapshotId,
    override val payload: DomainEvent,
    override val identifier: String = UUID.randomUUID().toString,
    override val metaData: Map[Any, Any] = Map.empty)
  extends EventMessage(payload, identifier, snapshotId.timestamp, metaData) {

  def aggregateId = snapshotId.aggregateId

  def sequenceNr = snapshotId.sequenceNr

  def withMetaData(newMetaData: Map[Any, Any], clearExisting: Boolean = false): DomainEventMessage = {
    if (clearExisting) {
      copy(metaData = newMetaData)
    } else {
      copy(metaData = metaData ++ newMetaData)
    }
  }

}