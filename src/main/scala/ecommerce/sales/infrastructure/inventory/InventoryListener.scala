package ecommerce.sales.infrastructure.inventory

import ecommerce.system.infrastructure.events.EventMessageListener

class InventoryListener extends EventMessageListener {
  override def endpointUri: String = "activemq:queue:Inventory"
}
