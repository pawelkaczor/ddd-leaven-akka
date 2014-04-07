package ddd.domain.event

object AggregateRootCreated {
  def apply(id: String) = new AggregateRootCreated(id)
  def unapply(e: AggregateRootCreated) = Option(e.id)
}

class AggregateRootCreated(val id: String) extends DomainEvent {

}
