package ddd.support.domain

import ddd.support.domain.Addressable.AddressResolver

object Addressable {

  type AddressResolver = PartialFunction[Any, String]

  /**
   * Implicit view.
   *
   * getAddress method will be "added" to any Addressable.
   *
   * In fact this method return object of anonymous class that provides method getAddress.
   * Thus getAddress method will not be called on Addressable but on newly created object
   * returned by this method.
   */
  implicit def addAddressable[T](implicit addressable: Addressable[T]) =
    new {
      def addressResolver = addressable.addressResolver
    }


}

trait Addressable[T] {
  def addressResolver: AddressResolver
  val domain: String
}



