package ecommerce.sales.domain.reservation

import akka.actor.{PoisonPill, ActorSystem}
import com.typesafe.config.ConfigFactory
import ecommerce.sales.domain.productscatalog.{ProductData, ProductType}
import ecommerce.sales.domain.reservation.Reservation._
import ecommerce.sales.domain.reservation.Reservation.ReserveProduct
import ecommerce.sales.domain.reservation.Reservation.CreateReservation
import ecommerce.sales.domain.reservation.Reservation.ReservationCreated
import ecommerce.sales.domain.reservation.Reservation.ProductReserved

import ReservationSpec._
import ecommerce.sales.sharedkernel.Money
import test.support.EventsourcedAggregateRootSpec
import ddd.support.domain.Representative._

object ReservationSpec {
  val testSystem = {
    val config = ConfigFactory.parseString(
      """akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.persistence.journal.plugin = "in-memory-journal"
      """.stripMargin)
    ActorSystem("OrderSpec", config)
  }
}

class ReservationSpec extends EventsourcedAggregateRootSpec[Reservation](testSystem) {

  override val aggregateRootId = "reservation1"

  "Reservation office" must {
    "handle Reservation process" in {
      val reservationId = aggregateRootId
      var reservationOffice = office[Reservation]

      expectEventPersisted[ReservationCreated] {
        reservationOffice ! CreateReservation(reservationId, "client1")
      }
      expectEventPersisted[ProductReserved] {
        reservationOffice ! ReserveProduct(reservationId, "product1", 1)
      }

      // kill reservation office and all its clerks (aggregate roots)
      reservationOffice ! PoisonPill
      Thread.sleep(1000)
      reservationOffice = office[Reservation]

      val product2 = ProductData("product2", "productName", ProductType.Standard, Money(10))
      val quantity= 1
      expectEventPersisted(ProductReserved(reservationId, product2, quantity)) {
        reservationOffice ! ReserveProduct(reservationId, "product2", quantity)
      }

      expectEventPersisted[ReservationClosed] {
        reservationOffice ! CloseReservation(reservationId)
      }

    }
  }

}
