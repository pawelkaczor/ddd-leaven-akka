package ecommerce.sales.domain.inventory

object ProductType extends Enumeration {
  type ProductType = Value
  val Standard, Food, Drug = Value
}
