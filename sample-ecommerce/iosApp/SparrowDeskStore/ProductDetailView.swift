import SwiftUI

struct ProductDetailView: View {
    let product: Product
    @StateObject private var cart = CartManager.shared
    @State private var showAddedToast = false

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Product image
                ZStack {
                    product.bgColor
                    Text(product.emoji)
                        .font(.system(size: 80))
                }
                .frame(height: 260)
                .frame(maxWidth: .infinity)

                VStack(alignment: .leading, spacing: 0) {
                    Text(product.name)
                        .font(.system(size: 24, weight: .bold))

                    Text(product.formattedPrice)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(Color(red: 0.106, green: 0.369, blue: 0.125))
                        .padding(.top, 8)

                    Text(product.description)
                        .font(.system(size: 15))
                        .foregroundColor(.secondary)
                        .lineSpacing(4)
                        .padding(.top, 16)

                    // Add to cart
                    Button(action: {
                        cart.add(product)
                        showAddedToast = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                            showAddedToast = false
                        }
                    }) {
                        Text("Add to Cart")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(Color(red: 0.106, green: 0.369, blue: 0.125))
                    .padding(.top, 24)

                    // Chat support
                    NavigationLink(destination: SupportChatView()) {
                        HStack(spacing: 6) {
                            Image(systemName: "bubble.left")
                            Text("Need help? Chat with our support team")
                        }
                        .font(.system(size: 14))
                        .foregroundColor(Color(red: 0.106, green: 0.369, blue: 0.125))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                    }
                    .padding(.top, 8)
                }
                .padding(20)
            }
        }
        .navigationTitle(product.name)
        .navigationBarTitleDisplayMode(.inline)
        .overlay {
            if showAddedToast {
                VStack {
                    Spacer()
                    Text("Added to cart!")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)
                        .background(Color.black.opacity(0.8))
                        .cornerRadius(8)
                        .padding(.bottom, 32)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .animation(.easeInOut(duration: 0.3), value: showAddedToast)
            }
        }
    }
}
