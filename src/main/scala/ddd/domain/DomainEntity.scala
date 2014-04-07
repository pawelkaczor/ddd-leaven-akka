package ddd.domain

object EntityStatus extends Enumeration {
  type EntityStatus = Value
  val Active, Archive = Value
}

import ddd.domain.EntityStatus._

// This "var" is on purpose!
abstract class DomainEntity(val id: String, var entityStatus: EntityStatus = Active)
