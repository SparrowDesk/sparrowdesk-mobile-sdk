import SwiftUI

struct ProductListView: View {
    @StateObject private var cart = CartManager.shared

    let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 12) {
                        ForEach(Product.catalog) { product in
                            NavigationLink(destination: ProductDetailView(product: product)) {
                                ProductCard(product: product)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(12)
                }

                // Chat FAB
                NavigationLink(destination: SupportChatView()) {
                    HStack(spacing: 8) {
                        Image(systemName: "bubble.left.fill")
                            .font(.system(size: 16))
                        Text("Chat with Support")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 14)
                    .background(Color(red: 0.106, green: 0.369, blue: 0.125)) // #1B5E20
                    .clipShape(Capsule())
                    .shadow(color: .black.opacity(0.2), radius: 4, y: 2)
                }
                .padding(16)
            }
            .navigationTitle("SparrowDesk Store")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: CartView()) {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: "cart")
                                .font(.system(size: 18))
                            if cart.count > 0 {
                                Text("\(cart.count)")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(width: 18, height: 18)
                                    .background(Color.red)
                                    .clipShape(Circle())
                                    .offset(x: 8, y: -8)
                            }
                        }
                    }
                }
            }
        }
        .tint(Color(red: 0.106, green: 0.369, blue: 0.125))
    }
}

struct ProductCard: View {
    let product: Product

    var body: some View {
        VStack(spacing: 0) {
            // Emoji image area
            ZStack {
                product.bgColor
                Text(product.emoji)
                    .font(.system(size: 48))
            }
            .frame(height: 140)

            // Info
            VStack(alignment: .leading, spacing: 4) {
                Text(product.name)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.primary)
                    .lineLimit(1)

                Text(product.formattedPrice)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(red: 0.106, green: 0.369, blue: 0.125))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
        }
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.08), radius: 2, y: 1)
    }
}
