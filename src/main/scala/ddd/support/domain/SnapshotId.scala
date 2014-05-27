package ddd.support.domain

import java.util.Date

case class SnapshotId(aggregateId: String, sequenceNr: Long = 0, timestamp: Date = new Date)
