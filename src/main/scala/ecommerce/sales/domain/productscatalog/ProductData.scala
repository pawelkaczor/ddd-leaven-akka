package ecommerce.sales.domain.productscatalog

import ecommerce.sales.domain.productscatalog.ProductType._
import ddd.domain.sharedkernel.Money
import java.util.Date

case class ProductData(
  productId: String,
  name: String,
  productType: ProductType,
  price: Money,
  snapshotDate: Date = new Date)
