package ddd.support.domain

case class SnapshotId(aggregateId: String, sequenceNr: Long = 0)
