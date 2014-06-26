package ecommerce.inventory.integration

import ecommerce.system.infrastructure.events.EventMessageConfirmableProducer
import akka.actor.Props
import akka.camel.Oneway

object InventoryQueue {
  val ExchangeName = "activemq:queue:Inventory"

  val name = "inventoryQueue"

  def props = Props(new InventoryQueue with Oneway)

}

class InventoryQueue extends EventMessageConfirmableProducer {
  override def endpointUri = InventoryQueue.ExchangeName
}
