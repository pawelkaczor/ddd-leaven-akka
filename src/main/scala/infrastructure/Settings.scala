package infrastructure

import _root_.akka.actor._
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {
  val BrokerUrl: String = config.getString("ecommerce.broker.url")
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)

  override def get(system: ActorSystem): SettingsImpl = super.get(system)
}