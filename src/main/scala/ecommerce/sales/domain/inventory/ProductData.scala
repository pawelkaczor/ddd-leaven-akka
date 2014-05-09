package ecommerce.sales.domain.inventory

import ecommerce.sales.domain.inventory.ProductType._
import java.util.Date
import ecommerce.sales.sharedkernel.Money

case class ProductData(
  productId: String,
  name: String,
  productType: ProductType,
  price: Money,
  snapshotDate: Date = new Date)
