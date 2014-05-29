package ddd.support.domain

import ddd.support.domain.Message.MetaData

object Message {
  type MetaData = scala.collection.mutable.Map[Any, Any]
}

abstract class Message(metaData: MetaData) extends Serializable {

  def withMetaData(metaData: Map[Any, Any], clearExisting: Boolean = false): Message = {
    if (clearExisting) {
      this.metaData.clear()
    }
    this.metaData.++=(metaData)
    this
  }

  def withMetaAttribute(attrName: Any, value: Any): Message = withMetaData(Map(attrName -> value))

  def hasMetaAttribute(attrName: Any) = metaData.get(attrName).isDefined

  def getMetaAttribute[B](attrName: Any) = tryGetMetaAttribute[B](attrName).get

  def tryGetMetaAttribute[B](attrName: Any): Option[B] = metaData.get(attrName).asInstanceOf[Option[B]]
}