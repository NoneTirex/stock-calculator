package pl.kliniewski.stock.calculator.nbp

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import pl.kliniewski.stock.calculator.LocalDateSerializer
import pl.kliniewski.stock.calculator.PriceSource
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object IsoLocalDateSerializer : LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE)

class NbpPriceSource(
    private val client: HttpClient,
) : PriceSource {
    override fun price(time: LocalDateTime, currency: String): BigDecimal? = run {
        if (currency == "PLN") {
            return@run 1f.toBigDecimal()
        }
        val date = time.toLocalDate().toString()
        val url = "https://api.nbp.pl/api/exchangerates/rates/A/$currency/$date/"
        val request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build()
        val response = client.send(request, BodyHandlers.ofString(Charsets.UTF_8))
        if (response.statusCode() == 404) {
            return@run null
        }
        val body = response.body()
        val currencyResponse = Json.decodeFromString<CurrencyResponse>(body)
        currencyResponse.rates[0].mid.toBigDecimal()
    }
}

@Serializable
data class CurrencyResponse(
    val table: String,
    val currency: String,
    val code: String,
    val rates: List<CurrencyRate>,
)

@Serializable
data class CurrencyRate(
    val no: String,
    @Serializable(with = IsoLocalDateSerializer::class)
    val effectiveDate: LocalDate,
    val mid: Float,
)