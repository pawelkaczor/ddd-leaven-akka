package test.support.broker

import org.scalatest.{Suite, BeforeAndAfterAll}

trait EmbeddedBrokerTestSupport extends EmbeddedBrokerRunner with BeforeAndAfterAll {
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
