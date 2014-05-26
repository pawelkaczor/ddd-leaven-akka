package ecommerce.sales.integration

import scala.slick.jdbc.JdbcBackend.Database
import ddd.support.domain.AggregateRoot.Event
import ecommerce.inventory.domain.Product.ProductAdded
import ecommerce.sales.domain.product.Product
import ecommerce.sales.productcatalog.ProductCatalog
import ecommerce.system.infrastructure.events.ProjectionSpec
import akka.actor.ActorSystem

class InventoryProjection(db: Database, productCatalog: ProductCatalog)(implicit system: ActorSystem) extends ProjectionSpec {

  override def apply(productId: String, event: Event) {
    event match {
      case ProductAdded(name, productType) =>
        db withSession { implicit session: productCatalog.profile.simple.Session =>
          productCatalog.insert(Product(productId, name, productType, price = None))
          log.info(s"Product $productId added to catalog")
        }
      }
  }

}
