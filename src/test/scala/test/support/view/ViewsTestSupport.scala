package test.support.view

import org.scalatest.{Suite, BeforeAndAfterAll}
import org.h2.tools.Server._
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend._

trait ViewsTestSupport extends BeforeAndAfterAll {
  this: Suite =>
  val h2Server = createTcpServer("-tcpPort", "8092", "-tcpAllowOthers")
  val dal = new Daos(H2Driver)
  val db = Database.forURL("jdbc:h2:tcp://localhost:8092/~/ecommerce", driver = "org.h2.Driver")

  override def beforeAll() {
    super.beforeAll()
    h2Server.start()
    import dal.profile.simple._

    db withSession { implicit session: Session =>
      dal.drop
      dal.create
    }

  }

  override def afterAll() {
    super.afterAll()
    h2Server.shutdown()
  }

}
