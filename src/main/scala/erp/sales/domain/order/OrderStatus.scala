package erp.sales.domain.order

object OrderStatus extends Enumeration {
  type OrderStatus = Value
  val Draft, Submitted, Archived = Value
}
