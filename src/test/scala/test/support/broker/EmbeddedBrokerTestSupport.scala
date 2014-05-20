package test.support.broker

import org.scalatest.{Suite, BeforeAndAfterAll}
import infrastructure.akka.broker.ActiveMQMessaging

trait EmbeddedBrokerTestSupport extends EmbeddedActiveMQRunner with ActiveMQMessaging with BeforeAndAfterAll {
  this: Suite =>

  override def beforeAll() {
    super.beforeAll()
    startBroker()
  }

  override def afterAll() {
    super.afterAll()
    stopBroker()
  }

}
