package ecommerce.system.infrastructure.events

import ddd.support.domain.event.{DomainEvent, DomainEventMessage}
import akka.event.LoggingAdapter
import ddd.support.domain.SnapshotId

abstract class ProjectionSpec(implicit val log: LoggingAdapter) extends Function[DomainEventMessage, Unit] {

  def currentVersion(aggregateId: String): Option[Long] = None

  def isApplied(event: DomainEventMessage) =
    currentVersion(event.aggregateId).flatMap(v => Some(v >= event.sequenceNr)).getOrElse(false)

  override def apply(event: DomainEventMessage): Unit =
    if (!isApplied(event))
      apply(event.snapshotId, event.event)

  def apply(snapshotId: SnapshotId, event: DomainEvent)
}
