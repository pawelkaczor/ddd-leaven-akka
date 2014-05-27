package ecommerce.sales.integration

import scala.slick.jdbc.JdbcBackend.Database
import ddd.support.domain.AggregateRoot.Event
import ecommerce.inventory.domain.Product.ProductAdded
import ecommerce.sales.domain.product.Product
import ecommerce.sales.productcatalog.ProductCatalog
import ecommerce.system.infrastructure.events.ProjectionSpec
import akka.event.LoggingAdapter
import ddd.support.domain.SnapshotId

class InventoryProjection(db: Database, productCatalog: ProductCatalog)
    (implicit override val log: LoggingAdapter)
  extends ProjectionSpec {

  import productCatalog.profile.simple._

  override def apply(snapshotId: SnapshotId, event: Event) {
    event match {
      case ProductAdded(name, productType) =>
        db withSession { implicit session: Session =>
          productCatalog.insert(Product(snapshotId, name, productType, price = None))
          log.info(s"Product $snapshotId added to catalog")
        }
      }
  }

  override def currentVersion(id: String): Option[Long] = {
    db withSession { implicit session: Session =>
      productCatalog.productById(id).map(p => p.snapshotId.sequenceNr)
    }
  }
}
