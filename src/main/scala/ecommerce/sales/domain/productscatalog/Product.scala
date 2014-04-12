package ecommerce.sales.domain.productscatalog

import ecommerce.sales.domain.productscatalog.ProductType.ProductType
import akka.actor.ActorLogging
import akka.persistence.EventsourcedProcessor
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain.{AggregateState, AggregateRoot}

class Product extends AggregateRoot[ProductState] with EventsourcedProcessor with ActorLogging {

  override def receiveCommand: Product#Receive = ???

  override def receiveRecover: Product#Receive = ???

  override val factory: AggregateRootFactory = ???
}

case class ProductState (
    id: String,
    name: String,
    productType: ProductType,
    price: Money)
  extends AggregateState {

  override def apply = {
    case _ => this
  }
}
