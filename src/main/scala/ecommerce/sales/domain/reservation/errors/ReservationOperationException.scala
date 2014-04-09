package ecommerce.sales.domain.reservation.errors

case class ReservationOperationException(message: String, reservationId: String) extends RuntimeException(message)