package ecommerce.sales.domain.reservation

import ddd.domain.DomainEntity
import ecommerce.sales.domain.productscatalog.ProductData
import java.util.UUID

case class ReservationItem(product: ProductData, quantity: Int) extends DomainEntity {

  override val id: String = UUID.randomUUID().toString

  def increaseQuantity(addedQuantity: Int) = copy(quantity = this.quantity + addedQuantity)

  def productId = product.productId
}

