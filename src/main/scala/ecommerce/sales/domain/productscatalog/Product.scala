package ecommerce.sales.domain.productscatalog

import ddd.domain.sharedkernel.Money
import ecommerce.sales.domain.productscatalog.ProductType.ProductType
import ddd.domain.{AggregateState, AggregateRoot}
import akka.actor.ActorLogging
import akka.persistence.EventsourcedProcessor
import ddd.domain.event.DomainEvent

class Product extends AggregateRoot[ProductState] with EventsourcedProcessor with ActorLogging {

  override def receiveCommand: Product#Receive = ???

  override def receiveRecover: Product#Receive = ???
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
