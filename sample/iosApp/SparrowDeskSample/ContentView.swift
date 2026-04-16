import SwiftUI
import SparrowDeskSDK

struct ContentView: View {
    @StateObject private var viewModel = WidgetViewModel()

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                if !viewModel.isLoaded {
                    configSection
                } else {
                    controlSection
                }

                // WebView container
                if viewModel.isLoaded {
                    WidgetWebView(sdk: viewModel.sdk!)
                        .opacity(viewModel.isWebViewVisible ? 1 : 0)
                        .frame(maxHeight: .infinity)
                }

                Spacer()
            }
            .navigationTitle("SparrowDesk Sample")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    // MARK: - Config Section

    private var configSection: some View {
        ScrollView {
            VStack(spacing: 12) {
                Group {
                    TextField("Domain (e.g. yourcompany.sparrowdesk.com)", text: $viewModel.domain)
                        .textContentType(.URL)
                        .autocapitalization(.none)

                    TextField("Widget Token", text: $viewModel.token)
                        .autocapitalization(.none)

                    TextField("User Email (optional)", text: $viewModel.email)
                        .textContentType(.emailAddress)
                        .autocapitalization(.none)

                    TextField("User Name (optional)", text: $viewModel.name)
                        .textContentType(.name)

                    TextField("Tags (comma-separated, optional)", text: $viewModel.tags)
                        .autocapitalization(.none)
                }
                .textFieldStyle(.roundedBorder)

                Button(action: { viewModel.loadWidget() }) {
                    Text("Load Widget")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                }
                .buttonStyle(.borderedProminent)
                .disabled(viewModel.domain.isEmpty || viewModel.token.isEmpty)
            }
            .padding()
        }
    }

    // MARK: - Control Section

    private var controlSection: some View {
        ScrollView {
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    Button("Open") { viewModel.sdk?.openWidget() ; viewModel.log("openWidget()") }
                        .buttonStyle(.bordered)
                    Button("Close") { viewModel.sdk?.closeWidget() ; viewModel.log("closeWidget()") }
                        .buttonStyle(.bordered)
                    Button("Hide Widget") { viewModel.sdk?.hideWidget() ; viewModel.log("hideWidget()") }
                        .buttonStyle(.bordered)
                }

                HStack(spacing: 8) {
                    Button("Show WebView") {
                        viewModel.sdk?.show()
                        viewModel.isWebViewVisible = true
                        viewModel.log("show()")
                    }
                    .buttonStyle(.bordered)

                    Button("Hide WebView") {
                        viewModel.sdk?.hide()
                        viewModel.isWebViewVisible = false
                        viewModel.log("hide()")
                    }
                    .buttonStyle(.bordered)

                    Button("Status") {
                        viewModel.sdk?.getStatus { status in
                            DispatchQueue.main.async {
                                viewModel.statusText = "Status: \(status.name)"
                            }
                        }
                    }
                    .buttonStyle(.bordered)
                }

                Button("Destroy", role: .destructive) {
                    viewModel.destroyWidget()
                }
                .buttonStyle(.bordered)

                Text(viewModel.statusText)
                    .font(.caption)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Text(viewModel.eventLog)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .lineLimit(5)
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
        .frame(maxHeight: 200)
    }
}

// MARK: - ViewModel

class WidgetViewModel: ObservableObject {
    @Published var domain = ""
    @Published var token = ""
    @Published var email = ""
    @Published var name = ""
    @Published var tags = ""
    @Published var isLoaded = false
    @Published var isWebViewVisible = true
    @Published var statusText = "Status: --"
    @Published var eventLog = "Events will appear here"

    var sdk: SparrowDeskSDK?

    func loadWidget() {
        let config = SparrowDeskConfig(domain: domain, token: token)
        let newSdk = SparrowDeskSDK(config: config)

        // Set contact fields
        var fields: [String: String] = [:]
        if !email.isEmpty { fields["email"] = email }
        if !name.isEmpty { fields["name"] = name }
        if !fields.isEmpty {
            newSdk.setContactFields(fields: fields)
            log("setContactFields: \(fields)")
        }

        // Set tags
        if !tags.isEmpty {
            let tagList = tags.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }
            newSdk.setTags(tags: tagList)
            log("setTags: \(tagList)")
        }

        // Register callbacks (SparrowDeskCallback is an ObjC protocol, so we wrap closures)
        newSdk.onOpen(callback: CallbackWrapper { [weak self] in
            DispatchQueue.main.async { self?.log("Event: Widget OPENED") }
        })
        newSdk.onClose(callback: CallbackWrapper { [weak self] in
            DispatchQueue.main.async { self?.log("Event: Widget CLOSED") }
        })

        sdk = newSdk
        isLoaded = true
        log("Widget loaded (domain=\(domain))")
    }

    func destroyWidget() {
        sdk?.destroy()
        sdk = nil
        isLoaded = false
        isWebViewVisible = true
        log("destroy() — SDK cleaned up")
    }

    func log(_ message: String) {
        let lines = eventLog.split(separator: "\n").suffix(4)
        eventLog = (lines + [Substring(message)]).joined(separator: "\n")
    }
}

// MARK: - WebView Wrapper

/// Wraps a Swift closure as a SparrowDeskCallback (ObjC protocol).
/// Kotlin `fun interface` exports as a protocol — Swift closures can't conform directly.
class CallbackWrapper: NSObject, SparrowDeskCallback {
    private let handler: () -> Void
    init(_ handler: @escaping () -> Void) { self.handler = handler }
    func onEvent() { handler() }
}

struct WidgetWebView: UIViewRepresentable {
    let sdk: SparrowDeskSDK

    func makeUIView(context: Context) -> UIView {
        let container = UIView()
        container.backgroundColor = UIColor.systemGray6
        sdk.attach(parentView: container)
        return container
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // No updates needed
    }
}
