package test.support.view

import scala.slick.driver.JdbcProfile
import ecommerce.sales.view.inventory.ProductDao
import infrastructure.view.Profile

class Daos(override val profile: JdbcProfile) extends ProductDao with Profile {
  import profile.simple._

  def drop(implicit session: Session): Unit = {
    products.ddl.drop
  }

  def create(implicit session: Session): Unit = {
    products.ddl.create
  }
}