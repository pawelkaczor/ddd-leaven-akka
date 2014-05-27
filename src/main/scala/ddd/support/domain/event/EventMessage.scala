package ddd.support.domain.event

import ddd.support.domain.AggregateRoot._
import java.util.Date

object EventMessage {
  val ReplyTo = "ReplyTo"
}

abstract class EventMessage(
    val payload: Event,
    val identifier: String,
    val timestamp: Date,
    val metaData: Map[String, Any])
  extends Serializable {

  def withMetaData(newMetaData: Map[String, Any], clearExisting: Boolean = false): EventMessage

  def withMetaAttribute(attrName: String, value: Any) = withMetaData(Map(attrName -> value))

  def hasMetaAttribute(attrName: String) = metaData.get(attrName).isDefined

  def getMetaAttribute[A](attrName: String) = metaData.get(attrName).get.asInstanceOf[A]
}