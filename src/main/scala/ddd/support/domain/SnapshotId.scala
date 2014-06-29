package ddd.support.domain

import ddd.support.domain.BusinessEntity.EntityId

case class SnapshotId(aggregateId: EntityId, sequenceNr: Long = 0)
