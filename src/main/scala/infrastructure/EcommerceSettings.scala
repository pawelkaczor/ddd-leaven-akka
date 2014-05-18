package infrastructure

import _root_.akka.actor._
import com.typesafe.config.Config

class EcommerceSettings(config: Config) extends Extension {
  val BrokerUrl: String = config.getString("ecommerce.broker.url")
  val ViewDbUrl: String = config.getString("ecommerce.view.db.url")
  val ViewDbDriver: String = config.getString("ecommerce.view.db.driver")
}

object EcommerceSettings extends ExtensionId[EcommerceSettings] with ExtensionIdProvider {

  override def lookup = EcommerceSettings

  override def createExtension(system: ExtendedActorSystem) =
    new EcommerceSettings(system.settings.config)

  override def get(system: ActorSystem): EcommerceSettings = super.get(system)
}