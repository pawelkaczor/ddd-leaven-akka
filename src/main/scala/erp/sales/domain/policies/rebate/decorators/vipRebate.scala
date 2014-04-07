package erp.sales.domain.policies.rebate.decorators

import ddd.domain.sharedkernel.Money
import erp.sales.domain.policies.rebate.Rebates.RebatePolicy

object vipRebate extends ((Money, Money) => Option[RebatePolicy] => RebatePolicy) {

  override def apply(minimalThreshold: Money, rebateValue: Money) =
    (innerPolicy) => {
      (product, quantity, regularCost) => {
        val baseValue = innerPolicy.map(_(product, quantity, regularCost)).getOrElse(regularCost)
        if (baseValue > minimalThreshold) baseValue - rebateValue else baseValue
      }
    }
}
