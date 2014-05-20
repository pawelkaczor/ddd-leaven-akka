package infrastructure.view

import scala.slick.jdbc.JdbcBackend._
import akka.actor.ActorSystem
import infrastructure.EcommerceSettings

trait ViewDatabase {
  val settings: EcommerceSettings

  lazy val viewDb = {
    Database.forURL(url = settings.ViewDbUrl, driver = settings.ViewDbDriver)
  }
  
}
