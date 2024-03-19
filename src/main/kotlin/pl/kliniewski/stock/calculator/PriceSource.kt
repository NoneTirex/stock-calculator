package pl.kliniewski.stock.calculator

import java.math.BigDecimal
import java.time.LocalDateTime

interface PriceSource {
    fun price(time: LocalDateTime, currency: String): BigDecimal?
}