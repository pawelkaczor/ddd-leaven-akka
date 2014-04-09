package ecommerce.sales.domain.offer.discounts

import ddd.domain.sharedkernel.Money

object StandardDiscount extends ((Double, Int) => Discounts.DiscountPolicy) {

  def apply(discount: Double, minimalQuantity: Int) = {
    (product, quantity, regularCost) => {
      val discountRation = BigDecimal(discount / 100)
      if (quantity >= minimalQuantity)
        regularCost * discountRation
      else
        Money(0)
    }
  }
}
