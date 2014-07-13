package infrastructure.cluster

import akka.actor.{ ActorSystem, PoisonPill, Props, ActorRef }
import akka.contrib.pattern.ClusterSingletonManager
import infrastructure.actor.CreationSupport

class SingletonManagerFactory(implicit system: ActorSystem) extends CreationSupport {

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
