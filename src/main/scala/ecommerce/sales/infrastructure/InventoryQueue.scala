package ecommerce.sales.infrastructure

import ecommerce.system.infrastructure.events.EventMessagesConfirmableProducer

class InventoryQueue extends EventMessagesConfirmableProducer {

  override def endpointUri: String = "activemq:queue:Inventory"

}
