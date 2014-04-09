package ecommerce.sales.domain.reservation

object ReservationStatus extends Enumeration {
  type ReservationStatus = Value
  val Opened, Closed = Value
}
