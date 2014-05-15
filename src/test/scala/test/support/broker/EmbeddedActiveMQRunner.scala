package test.support.broker

import org.apache.activemq.broker.BrokerService
import infrastructure.Settings
import akka.actor.ActorSystem
import java.io.IOException

trait EmbeddedActiveMQRunner{
  val system: ActorSystem

  val broker = new BrokerService()
  broker.setPersistent(false)

  def startBroker(retry: Int = 0) {
    try {
      broker.addConnector(Settings(system).BrokerUrl)
      broker.start()
      broker.waitUntilStarted()
    } catch {
      case ex: IOException =>
        if (retry < 5) {
          system.log.info("ActiveMQ is already running. Waiting until broker is stopped.")
          Thread.sleep(3000)
          startBroker(retry + 1)
        } else {
          throw ex
        }
    }
  }

  def stopBroker() {
    broker.stop()
  }
}
