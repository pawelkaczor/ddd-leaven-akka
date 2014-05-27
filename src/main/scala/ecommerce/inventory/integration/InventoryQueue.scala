package ecommerce.inventory.integration

import ecommerce.system.infrastructure.events.EventMessageConfirmableProducer
import akka.actor.Props
import akka.camel.Oneway

object InventoryQueue {
  val EndpointUri = "activemq:queue:Inventory"

  val name = "inventoryQueue"

  def recipeForInOnly = Props(new InventoryQueue with Oneway)

  def recipeForInOut = Props(new InventoryQueue)
}

class InventoryQueue extends EventMessageConfirmableProducer {
  override def endpointUri = InventoryQueue.EndpointUri
}
