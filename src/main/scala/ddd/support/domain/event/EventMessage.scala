package ddd.support.domain.event

import ddd.support.domain.AggregateRoot._
import java.util.Date

abstract class EventMessage(
    val payload: Event,
    val identifier: String,
    val timestamp: Date,
    val metaData: Map[String, AnyRef])
  extends Serializable {

  def withMetaData(newMetaData: Map[String, AnyRef]): EventMessage

}