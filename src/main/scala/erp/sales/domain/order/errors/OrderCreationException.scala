package erp.sales.domain.order.errors

case class OrderCreationException(message: String) extends RuntimeException(message)