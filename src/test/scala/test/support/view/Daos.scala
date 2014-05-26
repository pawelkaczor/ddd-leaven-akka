package test.support.view

import scala.slick.driver.JdbcProfile
import infrastructure.view.Profile
import ecommerce.sales.productcatalog.ProductCatalog

class Daos(override val profile: JdbcProfile) extends ProductCatalog with Profile {
  import profile.simple._

  def drop(implicit session: Session): Unit = {
    products.ddl.drop
  }

  def create(implicit session: Session): Unit = {
    products.ddl.create
  }
}