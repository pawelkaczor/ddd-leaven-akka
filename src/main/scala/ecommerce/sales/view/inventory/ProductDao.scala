package ecommerce.sales.view.inventory

import ecommerce.sales.domain.inventory.{ProductType, ProductData}
import infrastructure.view.Profile
import ecommerce.sales.sharedkernel.Money
import java.util.Currency

trait ProductDao extends Profile {
  import profile.simple._

  object Product extends ((String, String, String, BigDecimal, String) => Product){
    def apply(pd: ProductData) =
      new Product(pd.productId, pd.name, pd.productType.toString, pd.price.value, pd.price.currencyCode)
  }

  case class Product(id: String, name: String, pType: String, price: BigDecimal, currency: String) {
    def productData =
      ProductData(id, name, ProductType.withName(pType), Money(price, Currency.getInstance(currency)))
  }
  
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
    products.insert(Product(pd))
  }


  def productById(id: String)(implicit session: Session): Option[ProductData] = {
    //insert(ProductData(id, "product 1", ProductType.Standard, Money(10)))
    productByIdCompiled(id).run.headOption.map(p => p.productData)
  }

  val productByIdCompiled = {
    def query(productId: Column[String]) =
      for {
        p <- products if p.id === productId
      } yield p

    Compiled(query _)
  }
}