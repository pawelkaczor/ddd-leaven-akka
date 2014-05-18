package test.support.view

import org.scalatest.{Suite, BeforeAndAfterAll}
import org.h2.tools.Server._
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend._
import infrastructure.view.ViewDatabase

trait ViewsTestSupport extends ViewDatabase with BeforeAndAfterAll {
  this: Suite =>
  val h2Server = createTcpServer("-tcpPort", "8092", "-tcpAllowOthers")
  val dal = new Daos(H2Driver)

  override def beforeAll() {
    super.beforeAll()
    h2Server.start()
    import dal.profile.simple._

    viewDb withSession { implicit session: Session =>
      dal.drop
      dal.create
    }

  }

  override def afterAll() {
    super.afterAll()
    h2Server.shutdown()
  }

}
