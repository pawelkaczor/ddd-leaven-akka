package infrastructure.cluster

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.contrib.pattern.ShardRegion.Passivate
import ddd.support.domain._
import ecommerce.system.infrastructure.office.OfficeFactory
import infrastructure.actor.PassivationConfig

import scala.reflect.ClassTag

object ShardingSupport {
  implicit def globalOfficeFactory[A <: BusinessEntity](implicit ct: ClassTag[A], system: ActorSystem): OfficeFactory[A] = {
    new OfficeFactory[A] {
      private def region: Option[ActorRef] = {
        try {
          Some(ClusterSharding(system).shardRegion(officeName(ct)))
        } catch {
          case ex: IllegalArgumentException => None
        }
      }
      override def getOrCreate(caseIdResolution: IdResolution[A], clerkFactory: BusinessEntityActorFactory[A]): ActorRef = {
        implicit val sr: ShardResolution[A] = caseIdResolution.asInstanceOf[ShardResolution[A]]
        region.getOrElse {
          startSharding[A](ct, sr, system, clerkFactory)
          region.get
        }
      }
    }
  }

  def startSharding[A <: BusinessEntity](implicit ct: ClassTag[A], sr: ShardResolution[A],
    system: ActorSystem, actorFactory: BusinessEntityActorFactory[A]) {
    startSharding(sr)
  }

  def startSharding[A <: BusinessEntity](sr: ShardResolution[A])(implicit ct: ClassTag[A], system: ActorSystem, entFactory: BusinessEntityActorFactory[A]) {
    val entityProps = entFactory.props(new PassivationConfig(Passivate(PoisonPill), entFactory.inactivityTimeout))
    startSharding[A](sr, entityProps)
  }

  def startSharding[A](sr: ShardResolution[A], entryProps: Props)(implicit ct: ClassTag[A], system: ActorSystem) {
    val shardedClass = ct.runtimeClass.asInstanceOf[Class[A]]

    ClusterSharding(system).start(
      typeName = shardedClass.getSimpleName,
      entryProps = Some(entryProps),
      idExtractor = sr.idExtractor,
      shardResolver = sr.shardResolver)
  }

}