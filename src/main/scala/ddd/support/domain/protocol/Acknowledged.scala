package ddd.support.domain.protocol

import ddd.support.domain.Message

case object Acknowledged

case class Acknowledged(msg: Message)
