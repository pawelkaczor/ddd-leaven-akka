package erp.sales.domain.order.errors

case class OrderOperationException(
  message: String,
  orderId: String)
  extends RuntimeException(message)