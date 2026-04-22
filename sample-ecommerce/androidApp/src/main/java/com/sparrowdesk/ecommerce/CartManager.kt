package com.sparrowdesk.ecommerce

object CartManager {
    private val items = mutableListOf<Product>()

    fun add(product: Product) {
        items.add(product)
    }

    fun remove(product: Product) {
        items.remove(product)
    }

    fun getItems(): List<Product> = items.toList()

    fun getCount(): Int = items.size

    fun getTotal(): Double = items.sumOf { it.price }

    val formattedTotal: String get() = "$${String.format("%.2f", getTotal())}"

    fun clear() {
        items.clear()
    }
}
