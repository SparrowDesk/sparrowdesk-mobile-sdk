package com.sparrowdesk.ecommerce

import androidx.annotation.ColorRes

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val emoji: String,
    @ColorRes val bgColor: Int
) {
    val formattedPrice: String get() = "$${String.format("%.2f", price)}"

    companion object {
        val catalog = listOf(
            Product(
                id = 1,
                name = "Wireless Headphones",
                price = 79.99,
                description = "Premium noise-canceling wireless headphones with 30-hour battery life. " +
                    "Features active noise cancellation, transparency mode, and hi-res audio support. " +
                    "Comfortable memory foam ear cushions for all-day wear.",
                emoji = "\uD83C\uDFA7",
                bgColor = R.color.product_bg_1
            ),
            Product(
                id = 2,
                name = "Smart Watch",
                price = 199.99,
                description = "Feature-rich smartwatch with health and fitness tracking. " +
                    "Monitors heart rate, blood oxygen, sleep quality, and stress levels. " +
                    "Water-resistant to 50m with a bright always-on AMOLED display.",
                emoji = "⌚",
                bgColor = R.color.product_bg_2
            ),
            Product(
                id = 3,
                name = "Laptop Stand",
                price = 49.99,
                description = "Ergonomic aluminum laptop stand with adjustable height. " +
                    "Elevates your screen to eye level to reduce neck strain. " +
                    "Supports laptops up to 17 inches with non-slip silicone pads.",
                emoji = "\uD83D\uDCBB",
                bgColor = R.color.product_bg_3
            ),
            Product(
                id = 4,
                name = "USB-C Hub",
                price = 39.99,
                description = "7-in-1 USB-C multiport adapter for your laptop. " +
                    "Includes HDMI 4K@60Hz, USB 3.0 ports, SD/microSD card readers, " +
                    "and 100W Power Delivery pass-through charging.",
                emoji = "\uD83D\uDD0C",
                bgColor = R.color.product_bg_4
            )
        )

        fun findById(id: Int): Product? = catalog.find { it.id == id }
    }
}
