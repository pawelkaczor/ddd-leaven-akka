package test.support

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object TestConfig {
  def testSystem = {
    val config = ConfigFactory.parseString(
      """akka.loggers = ["akka.testkit.TestEventListener"]
        |ecommerce.broker.url = "nio://0.0.0.0:61616"
        |ecommerce.view.db.url = ""
        |ecommerce.view.db.driver = ""
        |akka.persistence.journal.plugin = "in-memory-journal"
      """.stripMargin)
    ActorSystem("Tests", config)
  }

}
