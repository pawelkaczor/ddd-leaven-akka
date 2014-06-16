package test.support.broker

import org.apache.activemq.broker.BrokerService
import infrastructure.EcommerceSettings
import java.io.IOException

trait EmbeddedActiveMQRunner {
  def settings: EcommerceSettings
  val broker = new BrokerService()
  broker.setPersistent(false)

  def startBroker(retry: Int = 0) {
    try {
      broker.addConnector(settings.BrokerUrl)
      broker.start()
      broker.waitUntilStarted()
    } catch {
      case ex: IOException =>
        if (retry < 5) {
          //system.log.info("ActiveMQ is already running. Waiting until broker is stopped.")
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
