package ecommerce.sales.domain.offer.discounts

import ecommerce.sales.domain.productscatalog.ProductData
import ecommerce.sales.domain.client.Client
import ecommerce.sales.sharedkernel.Money

object Discounts {
  type Quantity = Int
  type InitialCost = Money
  type DiscountAmount = Money
  type DiscountPolicyFactory = Client => DiscountPolicy
  type DiscountPolicy = (ProductData, Quantity, InitialCost) => DiscountAmount
}
