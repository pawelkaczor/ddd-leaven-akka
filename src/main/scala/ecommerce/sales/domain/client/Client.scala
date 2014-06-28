package ecommerce.sales.domain.client

import ddd.support.domain.BusinessEntity

case class Client(override val id: String) extends BusinessEntity
