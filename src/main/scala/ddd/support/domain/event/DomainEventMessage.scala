package ddd.support.domain.event

import ddd.support.domain.SnapshotId
import java.util.{Date, UUID}
import ddd.support.domain.Message._
import scala.collection.mutable.Map

case class DomainEventMessage(
    snapshotId: SnapshotId,
    override val payload: DomainEvent,
    override val identifier: String = UUID.randomUUID().toString,
    override val timestamp: Date = new Date,
    override val metaData: MetaData = Map.empty)
  extends EventMessage(payload, identifier, timestamp, metaData) {

  def this(em: EventMessage, s: SnapshotId) = this(s, em.payload, em.identifier, em.timestamp, em.metaData)

  def aggregateId = snapshotId.aggregateId

  def sequenceNr = snapshotId.sequenceNr

}