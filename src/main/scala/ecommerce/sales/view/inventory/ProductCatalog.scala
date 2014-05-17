package ecommerce.sales.view.inventory

import ecommerce.system.infrastructure.events.EventMessageListener
import scala.slick.jdbc.JdbcBackend.Database
import akka.actor.Props
import ecommerce.sales.domain.inventory.Product.ProductAdded

import ecommerce.sales.domain.inventory.ProductData
import ddd.support.domain.AggregateRoot.Event

object ProductCatalog {
  def props(db: Database, productDao: ProductDao) = Props(new ProductCatalog(db, productDao))
}

class ProductCatalog(db: Database, productDao: ProductDao) extends EventMessageListener {
  override def endpointUri: String = "activemq:queue:Inventory"

  override def handle(productId: String, event: Event) { event match {
    case ProductAdded(name, productType, price) =>
      db withSession { implicit session: productDao.profile.simple.Session =>
        productDao.insert(ProductData(productId, name, productType, price))
      }
    }
  }
}
