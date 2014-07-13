package infrastructure

import _root_.akka.actor.ActorSystem
import ddd.support.domain.AggregateIdResolution

package object cluster {

  class DefaultShardResolution[A] extends AggregateIdResolution[A] with ShardResolution[A]

  implicit def singletonManagerFactory(implicit system: ActorSystem) = new SingletonManagerFactory
}
