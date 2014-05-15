package ecommerce.sales.infrastructure.inventory

import ecommerce.system.infrastructure.events.EventMessagesConfirmableProducer

class InventoryQueue extends EventMessagesConfirmableProducer {

  override def endpointUri: String = "activemq:queue:Inventory"

}
