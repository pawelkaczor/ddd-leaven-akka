package ecommerce.sales.integration

import org.scalatest.{Matchers, WordSpecLike}
import ecommerce.inventory.domain.Product.ProductAdded
import test.support.view.{ViewsTestSupport, Daos}
import scala.slick.driver.H2Driver
import infrastructure.EcommerceSettings
import test.support.{RainbowLogger, TestConfig}
import ddd.support.domain.SnapshotId
import ecommerce.sales.sharedkernel.ProductType.Standard
import ddd.support.domain.event.DomainEventMessage

class InventoryProjectionTest extends WordSpecLike with Matchers with ViewsTestSupport {

  override val settings = new EcommerceSettings(TestConfig.config)
  implicit val logger = new RainbowLogger(suiteName)

  "InventoryProjection" should {
    "be idempotent" in {
      val daos: Daos = new Daos(H2Driver)
      val projection = new InventoryProjection(viewDb, daos)

      // given
      val message = DomainEventMessage(SnapshotId("product-1"), ProductAdded("product1", Standard))
      
      // when
      projection(message)
      projection(message)
      
      // then no exception
    }
  }

}
