package ddd.support.domain.event

import java.util.Date

object EventMessage {
  case object ReplyTo
  case object ReplyWith
}

abstract class EventMessage(
    val payload: DomainEvent,
    val identifier: String,
    val timestamp: Date,
    val metaData: Map[Any, Any])
  extends Serializable {

  def withMetaData(newMetaData: Map[Any, Any], clearExisting: Boolean = false): EventMessage

  def withMetaAttribute(attrName: Any, value: Any) = withMetaData(Map(attrName -> value))

  def hasMetaAttribute(attrName: Any) = metaData.get(attrName).isDefined

  def getMetaAttribute[A](attrName: Any) = metaData.get(attrName).get.asInstanceOf[A]
}