package ddd.support.domain.event

import java.util.Date
import ddd.support.domain.Message
import ddd.support.domain.Message.MetaData

abstract class EventMessage(
    val payload: DomainEvent,
    val identifier: String,
    val timestamp: Date,
    val metaData: MetaData)
  extends Message(metaData) {

}