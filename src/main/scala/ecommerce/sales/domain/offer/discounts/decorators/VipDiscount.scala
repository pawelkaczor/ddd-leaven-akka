package ecommerce.sales.domain.offer.discounts.decorators

import ecommerce.sales.domain.offer.discounts.Discounts
import Discounts.DiscountPolicy
import ecommerce.sales.sharedkernel.Money

object VipDiscount extends ((Money, Money) => Option[DiscountPolicy] => DiscountPolicy) {

  override def apply(minimalThreshold: Money, discountValue: Money) =
    (innerPolicy) => {
      (product, quantity, regularCost) => {
        val baseValue = innerPolicy.map(_(product, quantity, regularCost)).getOrElse(regularCost)
        if (baseValue > minimalThreshold) baseValue - discountValue else baseValue
      }
    }
}
