package ecommerce.sales.domain.reservation

import java.util.UUID
import ddd.support.domain.BusinessEntity
import ecommerce.sales.domain.product.Product

case class ReservationItem(product: Product, quantity: Int) extends BusinessEntity {

  override val id: String = UUID.randomUUID().toString

  def increaseQuantity(addedQuantity: Int) = copy(quantity = this.quantity + addedQuantity)

  def productId = product.productId
}

