package ecommerce.sales.view.inventory

import ecommerce.sales.domain.inventory.ProductData
import infrastructure.view.Profile

trait ProductDao extends Profile {
  import profile.simple._

  case class Product(id: String, name: String, pType: String, price: BigDecimal, currency: String)
  
  class Products(tag: Tag) extends Table[Product](tag, "PRODUCTS") {
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME", O.NotNull)
    def pType = column[String]("TYPE", O.NotNull)
    def price = column[BigDecimal]("PRICE", O.NotNull)
    def currency = column[String]("CURRENCY", O.NotNull)
    def * = (id, name, pType, price, currency) <> (Product.tupled, Product.unapply)
  }
  val products = TableQuery[Products]

  def insert(pd: ProductData)(implicit session: Session) {
    val p = Product(pd.productId, pd.name, pd.productType.toString, pd.price.value, pd.price.currencyCode)
    products.insert(p)
  }

}