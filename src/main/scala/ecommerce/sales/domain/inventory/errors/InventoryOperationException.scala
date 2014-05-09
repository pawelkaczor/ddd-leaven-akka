package ecommerce.sales.domain.inventory.errors

import ddd.support.domain.error.DomainException

case class InventoryOperationException(message: String, productId: String)
  extends RuntimeException(message) with DomainException {

}
