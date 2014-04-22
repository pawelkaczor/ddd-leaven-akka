package infrastructure.cluster

import akka.contrib.pattern.ShardRegion._
import ddd.support.domain.Addressable.AddressResolver
import ddd.support.domain.Addressable
import Shardable._

object Shardable {

  type ShardResolutionStrategy = AddressResolver => ShardResolver

  val defaultShardResolutionStrategy: ShardResolutionStrategy = {
    addressResolver => {
      case msg: Msg => Integer.toHexString(addressResolver(msg).hashCode).charAt(0).toString
    }
  }
}

trait Shardable[T] extends Addressable[T] {

  def shardResolutionStrategy = defaultShardResolutionStrategy

  val shardResolver: ShardResolver = shardResolutionStrategy(addressResolver)

  val idExtractor: IdExtractor = {
    case msg: Msg => (addressResolver(msg), msg)
  }

}

