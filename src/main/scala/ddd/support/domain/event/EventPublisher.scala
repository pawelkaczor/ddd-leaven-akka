package ddd.support.domain.event

trait EventPublisher extends EventHandler {

  override abstract def handle(event: DomainEvent): Unit = {
    publish(event)
    super.handle(event)
  }

  def publish(event: DomainEvent)
}
