package infrastructure.cluster

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.contrib.pattern.ShardRegion.Passivate
import ddd.support.domain.{ AggregateIdResolution, AggregateRoot, AggregateRootActorFactory }
import ecommerce.system.infrastructure.office.OfficeFactory
import infrastructure.actor.PassivationConfig

import scala.reflect.ClassTag

object ShardingSupport {
  implicit def globalOfficeFactory[T <: AggregateRoot[_]](implicit ct: ClassTag[T], system: ActorSystem): OfficeFactory[T] = {
    new OfficeFactory[T] {
      private def region: Option[ActorRef] = {
        try {
          Some(ClusterSharding(system).shardRegion(officeName(ct)))
        } catch {
          case ex: IllegalArgumentException => None
        }
      }
      override def getOrCreate(caseIdResolution: AggregateIdResolution[T], clerkFactory: AggregateRootActorFactory[T]): ActorRef = {
        println(caseIdResolution.getClass)
        implicit val sr: ShardResolution[T] = caseIdResolution.asInstanceOf[ShardResolution[T]]
        region.getOrElse {
          startSharding[T](ct, sr, system, clerkFactory)
          region.get
        }
      }
    }
  }

  def startSharding[T <: AggregateRoot[_]](implicit ct: ClassTag[T], sr: ShardResolution[T],
    system: ActorSystem, arActorFactory: AggregateRootActorFactory[T]) {
    startSharding(sr)
  }

  def startSharding[T <: AggregateRoot[_]](sr: ShardResolution[T])(implicit ct: ClassTag[T], system: ActorSystem, arFactory: AggregateRootActorFactory[T]) {
    val arProps = arFactory.props(new PassivationConfig(Passivate(PoisonPill), arFactory.inactivityTimeout))
    startSharding[T](sr, arProps)
  }

  def startSharding[T](sr: ShardResolution[T], entryProps: Props)(implicit ct: ClassTag[T], system: ActorSystem) {
    val shardedClass = ct.runtimeClass.asInstanceOf[Class[T]]

    ClusterSharding(system).start(
      typeName = shardedClass.getSimpleName,
      entryProps = Some(entryProps),
      idExtractor = sr.idExtractor,
      shardResolver = sr.shardResolver)
  }

}