package ddd.support.domain

import ddd.support.domain.event.DomainEvent

case class ExchangeSubscription(exchangeName: String, events: Class[DomainEvent]*)
