package test.support.view

import org.scalatest.{ Suite, BeforeAndAfterAll }
import org.h2.tools.Server._
import scala.slick.driver.H2Driver
import infrastructure.view.ViewDatabase
import akka.event.LoggingAdapter

trait ViewsTestSupport extends ViewDatabase with BeforeAndAfterAll {
  this: Suite =>
  val logger: LoggingAdapter

  val h2Server = createTcpServer("-tcpPort", "8092", "-tcpAllowOthers")
  val dal = new Daos(H2Driver)

  override def beforeAll() {
    super.beforeAll()

    logger.debug("Starting views")

    h2Server.start()
    import dal.profile.simple._

    viewDb withSession { implicit session: Session =>
      try {
        dal.drop
      } catch {
        case ex: Exception => // ignore
      }
      dal.create
    }

  }

  override def afterAll() {
    logger.debug("Stopping views")
    h2Server.stop()
    h2Server.shutdown()
    super.afterAll()
  }

}
