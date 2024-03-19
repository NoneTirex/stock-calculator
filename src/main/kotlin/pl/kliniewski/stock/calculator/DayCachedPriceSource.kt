package pl.kliniewski.stock.calculator

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class DayCachedPriceSource(
    val source: PriceSource
) : PriceSource {
    private val cache = HashMap<DayCacheKey, BigDecimal?>()
    override fun price(time: LocalDateTime, currency: String): BigDecimal? = run {
        cache.computeIfAbsent(DayCacheKey(time.toLocalDate(), currency)) {
            source.price(time, currency)
        }
    }
}

data class DayCacheKey(
    val date: LocalDate,
    val currency: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DayCacheKey) return false

        if (date != other.date) return false
        return currency == other.currency
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + currency.hashCode()
        return result
    }
}