package infrastructure.cluster

import scala.reflect.ClassTag
import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.contrib.pattern.ClusterSharding
import scala.Some
import akka.contrib.pattern.ShardRegion.Passivate
import scala.concurrent.duration._
import ddd.support.domain.AggregateRoot

trait ShardingSupport {

  def startSharding[T <: AggregateRoot[_]](implicit classTag: ClassTag[T], shardable: Shardable[T],
                           system: ActorSystem) {
    startSharding(shardable)
  }

  def startSharding[T <: AggregateRoot[_]](shardable: Shardable[T], inactivityTimeout: Duration = 1.minutes)
                               (implicit classTag: ClassTag[T], system: ActorSystem) {
    val arClass = classTag.runtimeClass.asInstanceOf[Class[T]]
    val arProps = Props(arClass, Passivate(stopMessage = PoisonPill), inactivityTimeout)

    ClusterSharding(system).start(
      typeName = shardable.domain,
      entryProps = Some(arProps),
      idExtractor = shardable.idExtractor,
      shardResolver = shardable.shardResolver
    )
  }

}
