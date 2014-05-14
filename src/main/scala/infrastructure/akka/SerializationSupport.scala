package infrastructure.akka

import akka.actor.ActorContext
import scala.reflect.ClassTag
import akka.serialization.SerializationExtension

trait SerializationSupport {

  def serialize(serializable: AnyRef)(implicit context: ActorContext) = {
    val serialization = SerializationExtension(context.system)
    val serializer = serialization.findSerializerFor(serializable)
    serializer.toBinary(serializable)
  }

  def deserialize[T](bytes: Array[Byte], serializedClass: Option[Class[_]] = None)(implicit context: ActorContext, classTag: ClassTag[T]): T = {
    val serialization = SerializationExtension(context.system)
    val serializer = serialization.serializerFor(serializedClass.getOrElse(classTag.runtimeClass))
    serializer.fromBinary(bytes, None).asInstanceOf[T]
  }

}
