package infrastructure.akka

import akka.actor.Actor

trait SerializationSupportForActor extends SerializationSupport {
  this: Actor =>

  implicit override def system = context.system
}
