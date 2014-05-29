package ecommerce.inventory.domain

import ecommerce.sales.sharedkernel.ProductType
import ProductType.ProductType
import ddd.support.domain._
import ddd.support.domain.event.{EventPublisher, DomainEvent}
import ecommerce.inventory.domain.errors.InventoryOperationException
import infrastructure.actor.PassivationConfig

object Product {

  def processorId(aggregateId: String) = "Products/" + aggregateId

  implicit val idResolution  = new ProductIdResolution

  class ProductIdResolution extends AggregateIdResolution[Product]

  // Commands
  sealed trait Command extends command.Command {
    def sku: String
    override def aggregateId = sku
  }
  case class AddProduct(sku: String, name: String, productType: ProductType) extends Command

  // Events
  case class ProductAdded(name: String, productType: ProductType) extends DomainEvent

}

import Product._
abstract class Product(override val passivationConfig: PassivationConfig) extends AggregateRoot[ProductState] {
  this: EventPublisher =>

  override def processorId = Product.processorId(aggregateId)

  override val factory: AggregateRootFactory = {
    case ProductAdded(name, productType) =>
      ProductState(name, productType)
  }

  override def handleCommand: Receive = {
    case AddProduct(productId, name, productType) =>
      if (initialized) {
        throw new InventoryOperationException(s"Product $productId already exists", productId)
      } else {
        raise(ProductAdded(name, productType))
      }
  }

}

case class ProductState (
    name: String,
    productType: ProductType)
  extends AggregateState {

  override def apply = {
    case _ => this
  }

}
