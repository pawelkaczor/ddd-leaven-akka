package ecommerce.sales.productcatalog

import scala.slick.jdbc.JdbcBackend.Database
import akka.actor.{Actor, ActorLogging, Props}
import ecommerce.sales.productcatalog.ProductFinder.GetProduct

object ProductFinder {
  def props(db: Database, productCatalog: ProductCatalog) = Props(new ProductFinder(db, productCatalog))
  val name = "productFinder"

  case class GetProduct(productId: String)
}

class ProductFinder(db: Database, productDao: ProductCatalog) extends Actor with ActorLogging {

  override def receive: Receive = {
    case GetProduct(productId) =>
      import productDao.profile.simple._
      db withSession { implicit session: Session =>
        sender ! productDao.productById(productId)
      }
  }
}
