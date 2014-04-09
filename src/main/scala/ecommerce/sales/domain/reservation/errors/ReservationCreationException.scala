package ecommerce.sales.domain.reservation.errors

case class ReservationCreationException(message: String) extends RuntimeException(message)