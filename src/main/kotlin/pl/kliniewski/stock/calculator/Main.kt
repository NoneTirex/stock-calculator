package pl.kliniewski.stock.calculator

import pl.kliniewski.stock.calculator.degiro.DegiroImporter
import pl.kliniewski.stock.calculator.nbp.NbpPriceSource
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.net.http.HttpClient
import java.time.LocalDateTime

fun checkCurrency(priceSource: PriceSource, time: LocalDateTime, currency: String, daysToAdd: Long): BigDecimal = run {
    priceSource.price(time, currency) ?: checkCurrency(priceSource, time.plusDays(daysToAdd), currency, daysToAdd)
}

data class StockEntryBatch(
    val closeEntry: StockEntry,
    val openEntries: List<StockEntry>,
)

fun packEntriesIntoBatches(entries: List<StockEntry>): List<StockEntryBatch> = run {
    val openEntries = ArrayDeque(entries.filter { it.count > 0 }.sortedBy { it.id })
    val closeEntries = entries.filter { it.count < 0 }.sortedBy { it.id }

    closeEntries.map { closeEntry ->
        var remaining = closeEntry.count
        val currentEntries = mutableListOf<StockEntry>()
        while (remaining < 0 && openEntries.size > 0) {
            val entry = openEntries.removeFirst()
            if (remaining + entry.count > 0) {
                val newCount = -remaining
                val unitPrice = entry.price.divide(entry.count.toBigDecimal(), 4, RoundingMode.HALF_UP)
                val newEntry = entry.copy(
                    count = newCount,
                    price = unitPrice.multiply(newCount.toBigDecimal())
                )
                currentEntries.add(newEntry)

                val oldCount = entry.count - newCount
                val oldEntry = entry.copy(
                    count = oldCount,
                    price = unitPrice.multiply(oldCount.toBigDecimal())
                )
                openEntries.addFirst(oldEntry)

                remaining += newEntry.count
            } else {
                currentEntries.add(entry)
                remaining += entry.count
            }
        }

        StockEntryBatch(
            closeEntry,
            currentEntries,
        )
    }
}

fun mapStockEntryToProcessedStockEntry(entry: StockEntry, priceSource: PriceSource, dayToAdd: Long): ProcessedStockEntry = run {
    val dayAfter = entry.time.plusDays(dayToAdd)

    val feeCurrencyPrice = entry.feeCurrency?.let { currency ->
        checkCurrency(priceSource, dayAfter, currency, dayToAdd)
    } ?: 1f.toBigDecimal()
    val currencyPrice = checkCurrency(priceSource, dayAfter, entry.currency, dayToAdd)

    ProcessedStockEntry(
        entry.id,
        entry.time,
        entry.product,
        entry.isin,
        entry.count,
        entry.fee.multiply(feeCurrencyPrice),
        entry.price.minus(entry.fee).multiply(currencyPrice)
    )
}

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Poprawne użycie: ./stock-calculator <format pliku> <ścieżka do pliku csv> <rok rozliczeniowy>")
        return
    }

    val fileFormat = args[0]
    val file = File(args[1])
    if (!file.exists()) {
        println("Nie znaleziono pliku csv")
        return
    }

    val year = args[2].toIntOrNull() ?: return println("Nieprawidłowy format roku")

    val importer = when(fileFormat.lowercase()) {
        "degiro" -> DegiroImporter(file)
        else -> return println("Nieprawidłowy format pliku csv, dostępne formaty: degiro")
    }
    val nbpPriceSource = NbpPriceSource(HttpClient.newHttpClient())
    val priceSource = DayCachedPriceSource(nbpPriceSource)

    val entries = importer.import()
    val result = entries.sortedBy { it.time }.asReversed().filter {
        it.time.year == year && it.count < 0
    }.map {
        Pair(it.product, it.isin)
    }.toSet().map { (product, isin) ->
        val productEntries = entries.filter {
            it.isin == isin
        }

        val batches = packEntriesIntoBatches(productEntries).filter { batch ->
            batch.closeEntry.time.year == year
        }

        val incomeEntries = batches.map {
            it.closeEntry
        }.map { mapStockEntryToProcessedStockEntry(it, priceSource, 1) }

        val costEntries = batches.flatMap {
            it.openEntries
        }.map { mapStockEntryToProcessedStockEntry(it, priceSource, -1) }

        val income = incomeEntries.sumOf {
            it.price
        }

        val cost = costEntries.sumOf {
            it.price.multiply((-1f).toBigDecimal()) + it.fee.multiply((-1f).toBigDecimal())
        } + incomeEntries.sumOf {
            it.fee.multiply((-1f).toBigDecimal())
        }

        CalculatedStock(
            product = product,
            isin = isin,
            income = income,
            cost = cost,
        )
    }
    result.forEach {
        println("${it.product} => przychód: ${it.income} => koszt: ${it.cost} => zysk/strata: ${it.income - it.cost}")
    }
    val income = result.sumOf { it.income }
    val cost = result.sumOf { it.cost }
    println("Przychód: ${String.format("%.2f", income)}")
    println("Koszt uzyskania przychodu: ${String.format("%.2f", cost)}")
}