package infrastructure.view

import scala.slick.driver.JdbcProfile

trait Profile {
  val profile: JdbcProfile
}
