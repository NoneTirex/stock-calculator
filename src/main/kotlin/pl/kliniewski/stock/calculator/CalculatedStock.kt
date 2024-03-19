package pl.kliniewski.stock.calculator

import java.math.BigDecimal

data class CalculatedStock(
    val product: String,
    val isin: String,
    val income: BigDecimal,
    val cost: BigDecimal,
)
