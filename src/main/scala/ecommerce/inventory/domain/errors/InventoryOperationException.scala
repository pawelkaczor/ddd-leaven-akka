package ecommerce.inventory.domain.errors

import ddd.support.domain.error.DomainException

case class InventoryOperationException(message: String, productId: String)
  extends RuntimeException(message) with DomainException {

}
