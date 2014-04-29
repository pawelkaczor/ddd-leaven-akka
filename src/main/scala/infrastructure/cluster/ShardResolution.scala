package infrastructure.cluster

import akka.contrib.pattern.ShardRegion._
import ddd.support.domain.AggregateIdResolution.AggregateIdResolver
import ddd.support.domain.AggregateIdResolution
import ShardResolution._

object ShardResolution {

  type ShardResolutionStrategy = AggregateIdResolver => ShardResolver

  val defaultShardResolutionStrategy: ShardResolutionStrategy = {
    aggregateIdResolver => {
      case msg: Msg => Integer.toHexString(aggregateIdResolver(msg).hashCode).charAt(0).toString
    }
  }
}

trait ShardResolution[T] extends AggregateIdResolution[T] {

  def shardResolutionStrategy = defaultShardResolutionStrategy

  val shardResolver: ShardResolver = shardResolutionStrategy(aggregateIdResolver)

  val idExtractor: IdExtractor = {
    case msg: Msg => (aggregateIdResolver(msg), msg)
  }

}

