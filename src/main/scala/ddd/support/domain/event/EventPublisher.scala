package ddd.support.domain.event

trait EventPublisher extends EventHandler {

  override abstract def handle(event: DomainEventMessage): Unit = {
    publish(event)
    super.handle(event)
  }

  def publish(event: DomainEventMessage)
}
