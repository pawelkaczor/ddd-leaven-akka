package ecommerce.sales.domain.reservation.errors

import ddd.support.domain.error.DomainException

case class ReservationOperationException(message: String, reservationId: String)
  extends RuntimeException(message) with DomainException