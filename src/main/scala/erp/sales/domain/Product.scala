package erp.sales.domain

import ddd.domain.sharedkernel.Money
import ProductType._

case class Product(
  id: Long,
  name: String,
  productType: ProductType,
  price: Money)