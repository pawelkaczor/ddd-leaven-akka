package ecommerce.sales.domain.product

import ecommerce.sales.sharedkernel.ProductType
import ProductType._
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain.SnapshotId

object Product {
  def apply(productId: String, name: String, productType: ProductType, price: Option[Money]) =
    new Product(SnapshotId(productId), name, productType, price)
}

case class Product(
  snapshotId: SnapshotId,
  name: String,
  productType: ProductType,
  price: Option[Money]) {

  def productId = snapshotId.aggregateId
  def version = snapshotId.sequenceNr
}
