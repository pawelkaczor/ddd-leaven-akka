package ecommerce.sales.infrastructure.inventory

import ecommerce.system.infrastructure.events.EventMessagesConfirmableProducer
import ecommerce.sales.infrastructure.inventory.InventoryQueue.EndpointUri
import akka.actor.Props

object InventoryQueue {
  val EndpointUri = "activemq:queue:Inventory"

  val props = Props[InventoryQueue]
  val name = "inventoryQueue"
}

class InventoryQueue extends EventMessagesConfirmableProducer {
  override def endpointUri = EndpointUri
}
