package erp.sales.domain.policies.rebate

import ddd.domain.sharedkernel.Money

object standardRebate extends ((Double, Int) => Rebates.RebatePolicy) {

  def apply(rebate: Double, minimalQuantity: Int) = {
    (product, quantity, regularCost) => {
      val rebateRatio = BigDecimal(rebate / 100)
      if (quantity >= minimalQuantity)
        regularCost * rebateRatio
      else
        Money(0)
    }
  }
}
