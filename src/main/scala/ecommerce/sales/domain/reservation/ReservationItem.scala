package ecommerce.sales.domain.reservation

import ecommerce.sales.domain.inventory.ProductData
import java.util.UUID
import ddd.support.domain.DomainEntity

case class ReservationItem(product: ProductData, quantity: Int) extends DomainEntity {

  override val id: String = UUID.randomUUID().toString

  def increaseQuantity(addedQuantity: Int) = copy(quantity = this.quantity + addedQuantity)

  def productId = product.productId
}

