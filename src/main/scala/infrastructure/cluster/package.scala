package infrastructure

import ecommerce.sales.domain.reservation.Reservation.ReservationIdResolution
import ecommerce.sales.domain.reservation.Reservation

package object cluster {

  // Reservation
  implicit val reservationShardResolution  = new ReservationShardResolution
  class ReservationShardResolution extends ReservationIdResolution with ShardResolution[Reservation]

}
