package erp.sales.domain.order

import erp.sales.domain.ProductType._
import ddd.domain.sharedkernel.Money
import ddd.domain.DomainEntity
import erp.sales.domain.order.Order.ProductAddedToOrder

object OrderProduct {
  def apply(event: ProductAddedToOrder): OrderProduct =
    new OrderProduct(event.productId, event.productType, event.price)

}

case class OrderProduct(
    override val id: String,
    productType: ProductType,
    price: Money)
  extends DomainEntity(id)
