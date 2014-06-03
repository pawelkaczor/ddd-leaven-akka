package ddd.support.domain.event

import java.util.{UUID, Date}
import ddd.support.domain.Message
import ddd.support.domain.Message.MetaData
import scala.collection.mutable.Map

class EventMessage(
    val event: DomainEvent,
    val identifier: String = UUID.randomUUID().toString,
    val timestamp: Date = new Date,
    val metaData: MetaData = Map.empty)
  extends Message(metaData) {

}