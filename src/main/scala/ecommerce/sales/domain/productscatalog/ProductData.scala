package ecommerce.sales.domain.productscatalog

import ecommerce.sales.domain.productscatalog.ProductType._
import java.util.Date
import ecommerce.sales.sharedkernel.Money

case class ProductData(
  productId: String,
  name: String,
  productType: ProductType,
  price: Money,
  snapshotDate: Date = new Date)
