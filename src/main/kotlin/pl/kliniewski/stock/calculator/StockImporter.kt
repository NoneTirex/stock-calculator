package pl.kliniewski.stock.calculator

interface StockImporter {
    fun import(): List<StockEntry>
}