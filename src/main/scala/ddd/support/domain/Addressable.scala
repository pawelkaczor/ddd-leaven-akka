package ddd.support.domain

object Addressable {

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
      def getAddress = addressable.getAddress
    }


}

trait Addressable[T] {
  def getAddress: PartialFunction[Any, String]
  val domain: String
}
