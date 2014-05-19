package ecommerce.sales.domain.offer.discounts

import ecommerce.sales.domain.client.Client
import ecommerce.sales.sharedkernel.Money
import ecommerce.sales.domain.product.Product

object Discounts {
  type Quantity = Int
  type InitialCost = Money
  type DiscountAmount = Money
  type DiscountPolicyFactory = Client => DiscountPolicy
  type DiscountPolicy = (Product, Quantity, InitialCost) => DiscountAmount
}
