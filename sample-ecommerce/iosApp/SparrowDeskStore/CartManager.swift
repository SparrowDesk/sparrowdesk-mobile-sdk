import SwiftUI

class CartManager: ObservableObject {
    static let shared = CartManager()

    @Published private(set) var items: [Product] = []

    var count: Int { items.count }
    var total: Double { items.reduce(0) { $0 + $1.price } }
    var formattedTotal: String { String(format: "$%.2f", total) }

    func add(_ product: Product) {
        items.append(product)
    }

    func remove(at index: Int) {
        items.remove(at: index)
    }

    func clear() {
        items.removeAll()
    }
}
