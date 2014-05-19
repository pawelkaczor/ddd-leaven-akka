package infrastructure

import ecommerce.sales.domain.reservation.Reservation.ReservationIdResolution
import ecommerce.sales.domain.reservation.Reservation
import ecommerce.inventory.domain.Product.ProductIdResolution
import ecommerce.inventory.domain.Product

package object cluster {

  // Reservation
  implicit val reservationShardResolution  = new ReservationShardResolution
  class ReservationShardResolution extends ReservationIdResolution with ShardResolution[Reservation]

  // Product
  implicit val productShardResolution  = new ProductShardResolution
  class ProductShardResolution extends ProductIdResolution with ShardResolution[Product]

}
