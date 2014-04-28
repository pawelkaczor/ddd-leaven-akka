package ddd.support.domain

import ddd.support.domain.Addressable.AddressResolver

object Addressable {
  type Command = Any
  type AggregateRootIdentifier = String
  type AddressResolver = PartialFunction[Command, AggregateRootIdentifier]
}

trait Addressable[T] {
  def addressResolver: AddressResolver
  val domain: String
}



