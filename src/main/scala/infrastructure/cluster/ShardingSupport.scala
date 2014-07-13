package infrastructure.cluster

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.contrib.pattern.ShardRegion.Passivate
import ddd.support.domain._
import ecommerce.system.infrastructure.office.OfficeFactory
import infrastructure.actor.PassivationConfig

import scala.reflect.ClassTag

object ShardingSupport {

  implicit def globalOfficeFactory[A <: BusinessEntity : ShardResolution : BusinessEntityActorFactory : ClassTag](implicit system: ActorSystem): OfficeFactory[A] = {
    new OfficeFactory[A] {
      private def region: Option[ActorRef] = {
        try {
          Some(ClusterSharding(system).shardRegion(officeName))
        } catch {
          case ex: IllegalArgumentException => None
        }
      }
      override def getOrCreate(implicit caseIdResolution: IdResolution[A]): ActorRef = {
        implicit val sr: ShardResolution[A] = caseIdResolution.asInstanceOf[ShardResolution[A]]
        region.getOrElse {
          startSharding()
          region.get
        }
      }

      private def startSharding(): Unit = {
        val entityFactory = implicitly[BusinessEntityActorFactory[A]]
        val entityProps = entityFactory.props(new PassivationConfig(Passivate(PoisonPill), entityFactory.inactivityTimeout))
        val entityClass = implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]]
        val sr = implicitly[ShardResolution[A]]

        ClusterSharding(system).start(
          typeName = entityClass.getSimpleName,
          entryProps = Some(entityProps),
          idExtractor = sr.idExtractor,
          shardResolver = sr.shardResolver)

      }

    }

  }

}