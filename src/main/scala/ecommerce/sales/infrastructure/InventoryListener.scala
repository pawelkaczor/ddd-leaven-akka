package ecommerce.sales.infrastructure

import ecommerce.system.infrastructure.events.EventMessageListener

class InventoryListener extends EventMessageListener {
  override def endpointUri: String = "activemq:queue:Inventory"
}
