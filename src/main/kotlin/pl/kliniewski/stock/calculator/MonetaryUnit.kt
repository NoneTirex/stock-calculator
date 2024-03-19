package pl.kliniewski.stock.calculator

import java.math.BigDecimal
import java.math.RoundingMode

data class MonetaryUnit(
        val value: BigDecimal,
) {
    fun multiply(multiplicand: MonetaryUnit): MonetaryUnit = run {
        MonetaryUnit(value.multiply(multiplicand.value).setScale(2, RoundingMode.HALF_UP))
    }

    fun divide(divisor: MonetaryUnit): MonetaryUnit = run {
        MonetaryUnit(value.divide(divisor.value, 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP))
    }

    fun add(augend: MonetaryUnit): MonetaryUnit = run {
        MonetaryUnit(value.add(augend.value).setScale(2, RoundingMode.HALF_UP))
    }
}
