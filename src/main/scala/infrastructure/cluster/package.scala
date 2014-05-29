package infrastructure

import ecommerce.sales.domain.reservation.Reservation
import ecommerce.inventory.domain.Product
import ddd.support.domain.AggregateIdResolution

package object cluster {

  // Reservation
  implicit val reservationShardResolution  = new ReservationShardResolution
  class ReservationShardResolution extends AggregateIdResolution[Reservation] with ShardResolution[Reservation]

  // Product
  implicit val productShardResolution  = new ProductShardResolution
  class ProductShardResolution extends AggregateIdResolution[Product] with ShardResolution[Product]

}
