package infrastructure

import _root_.akka.actor.{ PoisonPill, Props, ActorRef, ActorSystem }
import _root_.akka.contrib.pattern.ClusterSingletonManager
import ecommerce.sales.domain.reservation.Reservation
import ecommerce.inventory.domain.Product
import ddd.support.domain.AggregateIdResolution
import infrastructure.actor.CreationSupport

package object cluster {

  // Reservation
  implicit val reservationShardResolution = new ReservationShardResolution
  class ReservationShardResolution extends AggregateIdResolution[Reservation] with ShardResolution[Reservation]

  // Product
  implicit val productShardResolution = new ProductShardResolution
  class ProductShardResolution extends AggregateIdResolution[Product] with ShardResolution[Product]

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
