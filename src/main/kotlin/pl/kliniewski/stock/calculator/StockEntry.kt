package pl.kliniewski.stock.calculator

import java.math.BigDecimal
import java.time.LocalDateTime

data class StockEntry(
    val id: Long,
    val time: LocalDateTime,
    val product: String,
    val isin: String,
    val count: Int,
    val unitPrice: BigDecimal,
    val unitCurrency: String,
    val fee: BigDecimal,
    val feeCurrency: String?,
    val price: BigDecimal,
    val currency: String,
)

data class ProcessedStockEntry(
    val id: Long,
    val time: LocalDateTime,
    val product: String,
    val isin: String,
    val count: Int,
    val fee: BigDecimal,
    val price: BigDecimal,
)