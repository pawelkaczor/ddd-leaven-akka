package erp.sales.domain.policies.rebate

import ddd.domain.sharedkernel.Money
import erp.sales.domain.Client
import erp.sales.domain.order.OrderProduct


object Rebates {
  type Quantity = Int
  type InitialCost = Money
  type RebatedAmount = Money
  type RebatePolicyFactory = Client => RebatePolicy
  type RebatePolicy = (OrderProduct, Quantity, InitialCost) => RebatedAmount
}