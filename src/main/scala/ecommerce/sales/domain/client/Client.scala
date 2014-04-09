package ecommerce.sales.domain.client

import ddd.support.domain.DomainEntity

case class Client(override val id: String) extends DomainEntity
