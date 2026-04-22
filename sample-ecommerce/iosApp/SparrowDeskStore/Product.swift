import SwiftUI

struct Product: Identifiable {
    let id: Int
    let name: String
    let price: Double
    let description: String
    let emoji: String
    let bgColor: Color

    var formattedPrice: String {
        String(format: "$%.2f", price)
    }

    static let catalog: [Product] = [
        Product(
            id: 1,
            name: "Wireless Headphones",
            price: 79.99,
            description: "Premium noise-canceling wireless headphones with 30-hour battery life. Features active noise cancellation, transparency mode, and hi-res audio support. Comfortable memory foam ear cushions for all-day wear.",
            emoji: "🎧",
            bgColor: Color(red: 0.89, green: 0.95, blue: 0.99) // #E3F2FD
        ),
        Product(
            id: 2,
            name: "Smart Watch",
            price: 199.99,
            description: "Feature-rich smartwatch with health and fitness tracking. Monitors heart rate, blood oxygen, sleep quality, and stress levels. Water-resistant to 50m with a bright always-on AMOLED display.",
            emoji: "⌚",
            bgColor: Color(red: 0.95, green: 0.90, blue: 0.96) // #F3E5F5
        ),
        Product(
            id: 3,
            name: "Laptop Stand",
            price: 49.99,
            description: "Ergonomic aluminum laptop stand with adjustable height. Elevates your screen to eye level to reduce neck strain. Supports laptops up to 17 inches with non-slip silicone pads.",
            emoji: "💻",
            bgColor: Color(red: 0.91, green: 0.96, blue: 0.91) // #E8F5E9
        ),
        Product(
            id: 4,
            name: "USB-C Hub",
            price: 39.99,
            description: "7-in-1 USB-C multiport adapter for your laptop. Includes HDMI 4K@60Hz, USB 3.0 ports, SD/microSD card readers, and 100W Power Delivery pass-through charging.",
            emoji: "🔌",
            bgColor: Color(red: 1.0, green: 0.95, blue: 0.88) // #FFF3E0
        )
    ]

    static func findById(_ id: Int) -> Product? {
        catalog.first { $0.id == id }
    }
}
