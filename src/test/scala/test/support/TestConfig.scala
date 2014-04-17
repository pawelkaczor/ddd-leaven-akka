package test.support

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object TestConfig {
  def testSystem = {
    val config = ConfigFactory.parseString(
      """akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.persistence.journal.plugin = "in-memory-journal"
      """.stripMargin)
    ActorSystem("Tests", config)
  }

}
