package ecommerce.sales.domain.reservation

import akka.actor.{PoisonPill, Props, ActorSystem}
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

object ReservationSpec {
  val testSystem = {
    val config = ConfigFactory.parseString(
      """akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.persistence.journal.plugin = "in-memory-journal"
      """.stripMargin)
    ActorSystem("OrderSpec", config)
  }
}

class ReservationSpec extends EventsourcedAggregateRootSpec(testSystem) {

  override val aggregateRootId = "reservation1"

  def getReservationActor(name: String) = {
    getActor(Props[Reservation])(name)
  }

  "An Reservation actor" must {
    "handle Reservation process" in {
      val reservationId = aggregateRootId
      var reservation = getReservationActor(reservationId)

      expectEventLogged[ReservationCreated] {
        reservation ! CreateReservation(reservationId, "client1")
      }
      expectEventLogged[ProductReserved] {
        reservation ! ReserveProduct(reservationId, "product1", 1)
      }

      // kill and recreate reservation actor
      reservation ! PoisonPill
      Thread.sleep(1000)
      reservation = getReservationActor(reservationId)

      val product = ProductData("product2", "productName", ProductType.Standard, Money(10))
      val quantity= 2
      expectEventLogged(ProductReserved(reservationId, product, quantity)) {
        reservation ! ReserveProduct(reservationId, "product2", 2)
      }

      expectEventLogged[ReservationClosed] {
        reservation ! CloseReservation(reservationId)
      }

    }
  }

}
