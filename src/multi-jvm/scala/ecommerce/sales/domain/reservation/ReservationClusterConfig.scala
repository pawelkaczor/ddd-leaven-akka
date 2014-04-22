package ecommerce.sales.domain.reservation

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object ReservationClusterConfig extends MultiNodeConfig {

  val node1 = role("node1")
  val node2 = role("node2")

  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb-shared"
    akka.persistence.journal.leveldb-shared.store {
      native = off
      dir = "target/test-shared-journal"
    }
    akka.persistence.snapshot-store.local.dir = "target/test-snapshots"
                                         """))
}
