package infrastructure

import _root_.akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }
import _root_.akka.contrib.pattern.ClusterSingletonManager
import ddd.support.domain.AggregateIdResolution
import infrastructure.actor.CreationSupport

package object cluster {

  implicit def defaultShardResolution[A] = new DefaultShardResolution[A]
  class DefaultShardResolution[A] extends AggregateIdResolution[A] with ShardResolution[A]

  implicit def singletonManagerFactory(implicit system: ActorSystem): CreationSupport = {
    new CreationSupport {
      override def getChild(name: String): Option[ActorRef] = throw new UnsupportedOperationException
      override def createChild(props: Props, name: String): ActorRef = {
        system.actorOf(ClusterSingletonManager.props(
          singletonProps = props,
          singletonName = name,
          terminationMessage = PoisonPill,
          role = None),
          name = s"singletonOf$name")
      }
    }
  }

}
