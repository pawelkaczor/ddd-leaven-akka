package erp.sales.domain.order

import erp.sales.domain.policies.rebate.Rebates.RebatePolicy
import ddd.domain.sharedkernel.Money
import ddd.domain.DomainEntity

object OrderLine {
   def apply(product: OrderProduct, quantity: Int, rebatePolicy: RebatePolicy): OrderLine = {
     OrderLine(product.id, product, quantity, Money(0), Money(0)).applyPolicy(rebatePolicy)
   }
}

case class OrderLine(
    override val id: String,
    product: OrderProduct,
    quantity: Int,
    regularCost: Money,
    effectiveCost: Money)
  extends DomainEntity(id) {

  def applyPolicy(policy: RebatePolicy): OrderLine = {
    val newRegularCost = product.price * quantity
    val rebate = policy(product, quantity, regularCost)
    val newEffectiveCost = newRegularCost - rebate
    copy(regularCost = newRegularCost, effectiveCost = newEffectiveCost)
  }

  def increaseQuantity(addedQuantity: Int, policy: RebatePolicy): OrderLine =
    copy(quantity = this.quantity + addedQuantity).applyPolicy(policy)
}

