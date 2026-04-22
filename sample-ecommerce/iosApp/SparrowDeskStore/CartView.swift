import SwiftUI

struct CartView: View {
    @StateObject private var cart = CartManager.shared
    @State private var showOrderPlaced = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            if cart.items.isEmpty {
                emptyState
            } else {
                cartContent
            }
        }
        .navigationTitle("Your Cart")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Order placed! Thank you for your purchase.", isPresented: $showOrderPlaced) {
            Button("OK") { dismiss() }
        }
    }

    // MARK: - Empty State

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("🛒")
                .font(.system(size: 64))
            Text("Your cart is empty")
                .font(.system(size: 18))
                .foregroundColor(.secondary)
            Button("Browse Products") {
                dismiss()
            }
            .buttonStyle(.borderedProminent)
            .tint(Color(red: 0.106, green: 0.369, blue: 0.125))
            Spacer()
        }
    }

    // MARK: - Cart Content

    private var cartContent: some View {
        VStack(spacing: 0) {
            // Items list
            ScrollView {
                VStack(spacing: 0) {
                    ForEach(Array(cart.items.enumerated()), id: \.offset) { index, product in
                        HStack(spacing: 16) {
                            Text(product.emoji)
                                .font(.system(size: 28))

                            VStack(alignment: .leading, spacing: 2) {
                                Text(product.name)
                                    .font(.system(size: 16))
                                Text(product.formattedPrice)
                                    .font(.system(size: 14))
                                    .foregroundColor(Color(red: 0.106, green: 0.369, blue: 0.125))
                            }

                            Spacer()

                            Button("Remove") {
                                cart.remove(at: index)
                            }
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                        }
                        .padding(.vertical, 12)

                        if index < cart.items.count - 1 {
                            Divider()
                        }
                    }
                }
                .padding(16)
            }

            // Bottom section
            VStack(spacing: 12) {
                Divider()

                HStack {
                    Text("Subtotal")
                        .font(.system(size: 18, weight: .bold))
                    Spacer()
                    Text(cart.formattedTotal)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(red: 0.106, green: 0.369, blue: 0.125))
                }

                Button(action: {
                    cart.clear()
                    showOrderPlaced = true
                }) {
                    Text("Checkout")
                        .font(.system(size: 16, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
                .buttonStyle(.borderedProminent)
                .tint(Color(red: 0.106, green: 0.369, blue: 0.125))

                NavigationLink(destination: SupportChatView()) {
                    HStack(spacing: 6) {
                        Image(systemName: "bubble.left")
                        Text("Need help? Chat with our support team")
                    }
                    .font(.system(size: 14))
                    .foregroundColor(Color(red: 0.106, green: 0.369, blue: 0.125))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }
            }
            .padding(16)
            .background(Color(.systemBackground))
            .shadow(color: .black.opacity(0.05), radius: 4, y: -2)
        }
    }
}
