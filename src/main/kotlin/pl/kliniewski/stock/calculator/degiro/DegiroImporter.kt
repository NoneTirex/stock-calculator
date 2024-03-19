package pl.kliniewski.stock.calculator.degiro

import pl.kliniewski.stock.calculator.StockEntry
import pl.kliniewski.stock.calculator.StockImporter
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DegiroImporter(
    private val file: File
): StockImporter {
    override fun import(): List<StockEntry> = run {
        val lines = file.readLines(Charsets.UTF_8)
        var lastId = 0L
        lines.subList(1, lines.size).asReversed().map {
            val strings = it.split(",")
            val date = LocalDate.parse(strings[0], DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val time = LocalTime.parse(strings[1], DateTimeFormatter.ofPattern("HH:mm"))
            val dateTime = LocalDateTime.of(date, time)

            val product = strings[2]
            val isin = strings[3]
            val count = strings[6].toInt()

            val unitPrice = strings[7].toBigDecimal()
            val unitCurrency = strings[8]

            val fee = if (strings[14].isNotEmpty()) strings[14].toBigDecimal() else 0f.toBigDecimal()
            val feeCurrency = strings[15].ifEmpty { null }

            val price = strings[16].toBigDecimal()
            val currency = strings[17]

            StockEntry(
                id = lastId++,
                time = dateTime,
                product = product,
                isin = isin,
                count = count,
                unitPrice = unitPrice,
                unitCurrency =  unitCurrency,
                fee = fee,
                feeCurrency = feeCurrency,
                price = price,
                currency = currency,
            )
        }
    }
}