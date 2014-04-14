package ecommerce.sales.domain.reservation.errors

import ddd.support.domain.error.DomainException

case class ReservationCreationException(message: String)
  extends RuntimeException(message) with DomainException