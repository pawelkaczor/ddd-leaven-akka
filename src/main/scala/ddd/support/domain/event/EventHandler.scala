package ddd.support.domain.event

trait EventHandler {
  def handle(event: DomainEvent)
}
