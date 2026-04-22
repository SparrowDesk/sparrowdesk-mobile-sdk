import SwiftUI
import SparrowDeskSDK

struct SupportChatView: View {
    @StateObject private var viewModel = SupportChatViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack {
            // WebView — hidden until widget is open
            SparrowDeskWebView(sdk: viewModel.sdk)
                .opacity(viewModel.isWidgetReady ? 1 : 0)

            // Loading spinner
            if !viewModel.isWidgetReady {
                ProgressView()
                    .scaleEffect(1.2)
            }
        }
        .navigationTitle("Support Chat")
        .navigationBarTitleDisplayMode(.inline)
        .onChange(of: viewModel.shouldDismiss) { shouldDismiss in
            if shouldDismiss { dismiss() }
        }
        .onDisappear {
            viewModel.cleanup()
        }
    }
}

// MARK: - ViewModel

class SupportChatViewModel: ObservableObject {
    let sdk: SparrowDeskSDK
    @Published var isWidgetReady = false
    @Published var shouldDismiss = false

    init() {
        let config = SparrowDeskConfig(
            domain: AppConfig.sparrowDeskDomain,
            token: AppConfig.sparrowDeskToken,
            debug: false
        )
        sdk = SparrowDeskSDK(config: config)

        sdk.onOpen(callback: CallbackWrapper { [weak self] in
            DispatchQueue.main.async { self?.isWidgetReady = true }
        })
        sdk.onClose(callback: CallbackWrapper { [weak self] in
            DispatchQueue.main.async { self?.shouldDismiss = true }
        })
    }

    func cleanup() {
        sdk.destroy()
    }
}

// MARK: - WebView Wrapper

/// Wraps a Swift closure as a SparrowDeskCallback (ObjC protocol).
class CallbackWrapper: NSObject, SparrowDeskCallback {
    private let handler: () -> Void
    init(_ handler: @escaping () -> Void) { self.handler = handler }
    func onEvent() { handler() }
}

struct SparrowDeskWebView: UIViewRepresentable {
    let sdk: SparrowDeskSDK

    func makeUIView(context: Context) -> UIView {
        let container = UIView()
        container.backgroundColor = UIColor(white: 0.96, alpha: 1.0)
        sdk.attach(parentView: container)
        sdk.openWidget()
        sdk.hideWidget()
        return container
    }

    func updateUIView(_ uiView: UIView, context: Context) {}
}
