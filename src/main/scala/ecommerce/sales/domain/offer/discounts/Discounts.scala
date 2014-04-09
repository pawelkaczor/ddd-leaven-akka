package ecommerce.sales.domain.offer.discounts

import ddd.domain.sharedkernel.Money
import ecommerce.sales.domain.productscatalog.ProductData
import ecommerce.sales.domain.client.Client

object Discounts {
  type Quantity = Int
  type InitialCost = Money
  type DiscountAmount = Money
  type DiscountPolicyFactory = Client => DiscountPolicy
  type DiscountPolicy = (ProductData, Quantity, InitialCost) => DiscountAmount
}
