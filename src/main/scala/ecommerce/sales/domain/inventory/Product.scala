package ecommerce.sales.domain.inventory

import ecommerce.sales.domain.inventory.ProductType.ProductType
import akka.actor.Props
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain._
import ddd.support.domain.event.DomainEvent
import ecommerce.sales.domain.inventory.errors.InventoryOperationException
import infrastructure.actor.PassivationConfig

object Product {

  implicit val idResolution  = new ProductIdResolution

  class ProductIdResolution extends AggregateIdResolution[Product] {
    override def aggregateIdResolver = {
      case cmd: Command => cmd.productId
    }
  }

  // Commands
  sealed trait Command { def productId: String }
  case class AddProduct(productId: String, name: String, productType: ProductType, price: Money) extends Command

  // Events
  case class ProductAdded(productId: String, name: String, productType: ProductType, price: Money) extends DomainEvent

}

import Product._
abstract class Product(override val passivationConfig: PassivationConfig) extends AggregateRoot[ProductState] {
  this: EventPublisher =>

  override val factory: AggregateRootFactory = {
    case ProductAdded(_, name, productType, price) =>
      ProductState(name, productType, price)
  }

  override def handleCommand: Receive = {
    case cmd: Command => cmd match {
      case AddProduct(productId, name, productType, price) =>
        if (initialized) {
          throw new InventoryOperationException(s"Product $productId already exists", productId)
        } else {
          raise(ProductAdded(productId, name, productType, price))
        }
    }
  }

}

case class ProductState (
    name: String,
    productType: ProductType,
    price: Money)
  extends AggregateState {

  override def apply = {
    case _ => this
  }

}
