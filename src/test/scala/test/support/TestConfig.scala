package test.support

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object TestConfig {
  val config = ConfigFactory.parseString(
    """akka.loggers = ["akka.testkit.TestEventListener"]
      |akka.actor.debug.autoreceive = "on"
      |ecommerce.broker.url = "nio://0.0.0.0:61616"
      |ecommerce.view.db.url = "jdbc:h2:tcp://localhost:8092/~/ecommerce"
      |ecommerce.view.db.driver = "org.h2.Driver"
      |akka.persistence.journal.plugin = "in-memory-journal"
    """.stripMargin)

  def testSystem = {
    ActorSystem("Tests", config)
  }

}
