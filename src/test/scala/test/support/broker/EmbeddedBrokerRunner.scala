package test.support.broker

import org.apache.activemq.broker.BrokerService

trait EmbeddedBrokerRunner{
  val broker = new BrokerService()
  broker.addConnector("nio://0.0.0.0:61616")
  broker.setPersistent(false)

  def startBroker() {
    broker.start()
    broker.waitUntilStarted()
  }

  def stopBroker() {
    broker.stop()
    broker.waitUntilStopped()
  }
}
