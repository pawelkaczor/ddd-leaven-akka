package ddd.support.domain.event

import java.util.UUID
import ddd.support.domain.SnapshotId
import ddd.support.domain.Message.MetaData
import scala.collection.mutable.Map

case class DomainEventMessage(
    snapshotId: SnapshotId,
    override val payload: DomainEvent,
    override val identifier: String = UUID.randomUUID().toString,
    override val metaData: MetaData = Map.empty)
  extends EventMessage(payload, identifier, snapshotId.timestamp, metaData) {

  def aggregateId = snapshotId.aggregateId

  def sequenceNr = snapshotId.sequenceNr

}