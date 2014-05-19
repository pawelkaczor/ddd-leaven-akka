package ecommerce.sales.service

import ecommerce.system.infrastructure.events.EventListener
import scala.slick.jdbc.JdbcBackend.Database
import akka.actor.{ActorLogging, Props}
import ddd.support.domain.AggregateRoot.Event
import ecommerce.inventory.domain.Product.ProductAdded
import ecommerce.sales.domain.product.Product
import ecommerce.sales.service.ProductCatalog.GetProduct
import ecommerce.inventory.integration.InventoryQueue

object ProductCatalog {
  def props(db: Database, productDao: ProductDao) = Props(new ProductCatalog(db, productDao))
  val name = "productCatalog"

  case class GetProduct(productId: String)
}

class ProductCatalog(db: Database, productDao: ProductDao) extends EventListener with ActorLogging {
  override def endpointUri: String = InventoryQueue.EndpointUri

  override def handle(productId: String, event: Event) {
    event match {
      case ProductAdded(name, productType) =>
        db withSession { implicit session: productDao.profile.simple.Session =>
          productDao.insert(Product(productId, name, productType, price = None))
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
