package ecommerce.sales.view.inventory

import ecommerce.system.infrastructure.events.EventListener
import scala.slick.jdbc.JdbcBackend.Database
import akka.actor.{ActorLogging, Props}
import ecommerce.sales.domain.inventory.Product.ProductAdded

import ecommerce.sales.domain.inventory.ProductData
import ddd.support.domain.AggregateRoot.Event
import ecommerce.sales.view.inventory.ProductCatalog.GetProduct
import ecommerce.sales.infrastructure.inventory.InventoryQueue
import pl.project13.scala.rainbow
import rainbow._

object ProductCatalog {
  def props(db: Database, productDao: ProductDao) = Props(new ProductCatalog(db, productDao))
  val name = "productCatalog"

  case class GetProduct(productId: String)
}

class ProductCatalog(db: Database, productDao: ProductDao) extends EventListener with ActorLogging {
  override def endpointUri: String = InventoryQueue.EndpointUri

  override def handle(productId: String, event: Event) {
    event match {
      case ProductAdded(name, productType, price) =>
        db withSession { implicit session: productDao.profile.simple.Session =>
          productDao.insert(ProductData(productId, name, productType, price))
          log.info(s"Product $productId added to catalog")
        }
      }
  }


  override def unhandled(message: Any) { message match {
      case GetProduct(productId) =>
        import productDao.profile.simple._
        db withSession { implicit session: Session =>
          sender() ! productDao.productById(productId)
        }
      case _ =>
        super.unhandled(message)
    }
  }

}
