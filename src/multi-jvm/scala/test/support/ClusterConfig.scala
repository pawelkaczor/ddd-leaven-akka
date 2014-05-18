package test.support

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object ClusterConfig extends MultiNodeConfig {

  val node1 = role("node1")
  val node2 = role("node2")

  commonConfig(ConfigFactory.parseString("""
    ecommerce.broker.url = "nio://0.0.0.0:61616"
    ecommerce.view.db.url = "jdbc:h2:tcp://localhost:8092/~/ecommerce"
    ecommerce.view.db.driver = "org.h2.Driver"
    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb-shared"
    akka.persistence.journal.leveldb-shared.store {
      native = off
      dir = "target/test-shared-journal"
    }
    akka.persistence.snapshot-store.local.dir = "target/test-snapshots"
                                         """).withFallback(ConfigFactory.load()))
}
