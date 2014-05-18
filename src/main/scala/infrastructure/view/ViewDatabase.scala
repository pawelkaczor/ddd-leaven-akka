package infrastructure.view

import scala.slick.jdbc.JdbcBackend._
import akka.actor.ActorSystem
import infrastructure.EcommerceSettings

trait ViewDatabase {
  val system: ActorSystem
  val settings: EcommerceSettings

  def viewDb = Database.forURL(url = settings.ViewDbUrl, driver = settings.ViewDbDriver)
  
}
