package infrastructure.akka

import akka.actor.ActorSystem
import scala.reflect.ClassTag
import akka.serialization.SerializationExtension

trait SerializationSupport {
  implicit protected def system: ActorSystem

  def serialize(serializable: AnyRef)(implicit system: ActorSystem) = {
    val serialization = SerializationExtension(system)
    val serializer = serialization.findSerializerFor(serializable)
    serializer.toBinary(serializable)
  }

  def deserialize[T](bytes: Array[Byte], serializedClass: Option[Class[_]] = None)(implicit system: ActorSystem, classTag: ClassTag[T]): T = {
    val serialization = SerializationExtension(system)
    val serializer = serialization.serializerFor(serializedClass.getOrElse(classTag.runtimeClass))
    serializer.fromBinary(bytes, None).asInstanceOf[T]
  }

}
