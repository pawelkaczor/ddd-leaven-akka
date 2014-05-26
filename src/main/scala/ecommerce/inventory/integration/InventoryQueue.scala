package ecommerce.inventory.integration

import ecommerce.system.infrastructure.events.EventMessageConfirmableProducer
import akka.actor.Props
import akka.camel.Oneway

object InventoryQueue {
  val EndpointUri = "activemq:queue:Inventory"

  val name = "inventoryQueue"

  def recipeForInOnly(applicationLevelAck: Boolean = false) =
    Props(new InventoryQueue(applicationLevelAck) with Oneway)

  def recipeForInOut(applicationLevelAck: Boolean = false) =
    Props(new InventoryQueue(applicationLevelAck))
}

class InventoryQueue(applicationLevelAck: Boolean) extends EventMessageConfirmableProducer(applicationLevelAck) {
  override def endpointUri = InventoryQueue.EndpointUri
}
