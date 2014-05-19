package ecommerce.inventory.integration

import ecommerce.system.infrastructure.events.EventMessageConfirmableProducer
import akka.actor.Props

object InventoryQueue {
  val EndpointUri = "activemq:queue:Inventory"

  val props = Props[InventoryQueue]
  val name = "inventoryQueue"
}

class InventoryQueue extends EventMessageConfirmableProducer {
  override def endpointUri = InventoryQueue.EndpointUri
}
