package ddd.support.domain.command

import java.util.{UUID, Date}
import ddd.support.domain.Message.MetaData
import ddd.support.domain.Message
import scala.collection.mutable.Map

case class CommandMessage(
    command: Command,
    identifier: String = UUID.randomUUID().toString,
    timestamp: Date = new Date,
    metaData: MetaData = Map.empty)
  extends Message(metaData) {

}