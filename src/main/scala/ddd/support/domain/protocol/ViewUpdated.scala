package ddd.support.domain.protocol

import ddd.support.domain.event.DomainEvent

case class ViewUpdated(event: DomainEvent) extends Receipt
