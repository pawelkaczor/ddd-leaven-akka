package infrastructure.cluster

import scala.reflect.ClassTag
import akka.actor.{Props, ActorSystem}
import akka.contrib.pattern.ClusterSharding
import scala.Some

trait ShardingSupport {

  def startSharding[T](implicit classTag: ClassTag[T], shardable: Shardable[T],
                           system: ActorSystem) {
    startSharding(shardable)
  }

  def startSharding[T](shardable: Shardable[T])(implicit classTag: ClassTag[T],
                       system: ActorSystem) {
    ClusterSharding(system).start(
      typeName = shardable.domain,
      entryProps = Some(Props(classTag.runtimeClass.asInstanceOf[Class[T]])),
      idExtractor = shardable.idExtractor,
      shardResolver = shardable.shardResolver
    )
  }

}
