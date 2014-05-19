package ecommerce.sales.service

import infrastructure.view.Profile
import ecommerce.sales.sharedkernel.{ProductType, Money}
import java.util.Currency
import ecommerce.sales.domain.product.Product

trait ProductDao extends Profile {
  import profile.simple._

  object ProductRow extends ((String, String, String, Option[BigDecimal], Option[String]) => ProductRow){
    def apply(p: Product) = {
      val priceOpt = p.price.flatMap(m => Some(m.value))
      val currencyOpt = p.price.flatMap(m => Some(m.currencyCode))
      new ProductRow(p.productId, p.name, p.productType.toString, priceOpt, currencyOpt)
    }
  }

  case class ProductRow(id: String, name: String, pType: String, price: Option[BigDecimal], currency: Option[String]) {
    def productData =
      Product(id, name, ProductType.withName(pType), priceAsMoney)

    def priceAsMoney: Option[Money] = {
      if (price.isDefined) {
        Some(Money(price.get, Currency.getInstance(currency.get)))
      } else {
        None
      }
    }
  }
  
  class Products(tag: Tag) extends Table[ProductRow](tag, "PRODUCTS") {
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME", O.NotNull)
    def pType = column[String]("TYPE", O.NotNull)
    def price = column[Option[BigDecimal]]("PRICE")
    def currency = column[Option[String]]("CURRENCY")
    def * = (id, name, pType, price, currency) <> (ProductRow.tupled, ProductRow.unapply)
  }
  val products = TableQuery[Products]

  def insert(pd: Product)(implicit session: Session) {
    products.insert(ProductRow(pd))
  }


  def productById(id: String)(implicit session: Session): Option[Product] = {
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