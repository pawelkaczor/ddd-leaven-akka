package ecommerce.sales.domain.product

import ecommerce.sales.sharedkernel.ProductType
import ProductType._
import java.util.Date
import ecommerce.sales.sharedkernel.Money

case class Product(
  productId: String,
  name: String,
  productType: ProductType,
  price: Option[Money],
  snapshotDate: Date = new Date)
