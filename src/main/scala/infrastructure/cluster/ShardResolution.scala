package infrastructure.cluster

import akka.contrib.pattern.ShardRegion._
import ddd.support.domain.{ EntityMessage, IdResolution }
import ddd.support.domain.IdResolution.EntityIdResolver
import ddd.support.domain.command.{ Command, CommandMessage }
import infrastructure.cluster.ShardResolution._

object ShardResolution {

  type ShardResolutionStrategy = EntityIdResolver => ShardResolver

  val defaultShardResolutionStrategy: ShardResolutionStrategy = {
    entityIdResolver =>
      {
        case msg => Integer.toHexString(entityIdResolver(msg).hashCode).charAt(0).toString
      }
  }
}

trait ShardResolution[A] extends IdResolution[A] {

  def shardResolutionStrategy = defaultShardResolutionStrategy

  val shardResolver: ShardResolver = shardResolutionStrategy(entityIdResolver)

  val idExtractor: IdExtractor = {
    case em: EntityMessage => (entityIdResolver(em), em)
    case c: Command => (entityIdResolver(c), CommandMessage(c))
  }

}

