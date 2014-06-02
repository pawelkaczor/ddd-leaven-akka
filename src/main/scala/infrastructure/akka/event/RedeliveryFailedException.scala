package infrastructure.akka.event

import ddd.support.domain.event.DomainEventMessage

case class RedeliveryFailedException(event: DomainEventMessage) extends RuntimeException
