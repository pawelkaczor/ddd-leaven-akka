package ecommerce.sales.domain.productscatalog

import ecommerce.sales.domain.productscatalog.ProductType.ProductType
import akka.actor.{PoisonPill, ActorLogging}
import akka.persistence.EventsourcedProcessor
import ecommerce.sales.sharedkernel.Money
import ddd.support.domain.{AggregateState, AggregateRoot}
import infrastructure.actor.{Passivate, PassivationConfig}

object Product {

  import scala.concurrent.duration._
  val passivationConfig: PassivationConfig = PassivationConfig(Passivate(PoisonPill), 1 minutes)

}
import Product._
class Product extends AggregateRoot[ProductState](passivationConfig) with EventsourcedProcessor with ActorLogging {

  override def handleCommand: Product#Receive = ???

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
