package test.support

import ddd.support.domain.{AggregateState, AggregateRoot}
import infrastructure.actor.PassivationConfig
import ddd.support.domain.command.Command
import ddd.support.domain.event.DomainEvent
import test.support.DummyAggregateRoot.{Create, Created}

object DummyAggregateRoot {

  // commands
  case class Create(name: String = "dummy") extends Command {
    override def aggregateId: String = name
  }

  // events
  case class Created(name: String = "dummy") extends DomainEvent
}

class DummyAggregateRoot extends AggregateRoot[DummyState] {

  override val factory: AggregateRootFactory = {
    case Created(name) => DummyState(name)
  }

  override def handleCommand: Receive = {
    case Create(name) => raise(Created(name))
  }

  override val passivationConfig: PassivationConfig = PassivationConfig()
}

case class DummyState(name: String) extends AggregateState {
  override def apply: StateMachine = throw new UnsupportedOperationException
}
