# SparrowDesk Mobile SDK

Kotlin Multiplatform SDK for embedding the [SparrowDesk](https://sparrowdesk.com) chat widget in Android and iOS apps.

The SDK loads the widget inside a native WebView and provides a Kotlin/Swift API to control it programmatically — open/close the chat, identify users, set tags, and listen to events.

## Platform Support

| Platform | Min Version | Implementation |
|----------|------------|----------------|
| Android  | API 24     | `WebView` + `@JavascriptInterface` |
| iOS      | 16.0       | `WKWebView` + `WKScriptMessageHandler` |

## Installation

### Android (JitPack)

1. Add the JitPack repository to your **`settings.gradle.kts`**:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

2. Add the dependency to your **`build.gradle.kts`**:

```kotlin
dependencies {
    implementation("com.github.SparrowDesk.sparrowdesk-mobile-sdk:sparrowdesk-sdk:0.1.1")
}
```

3. The SDK declares the `INTERNET` permission in its own manifest — no need to add it yourself.

### iOS (Swift Package Manager)

**In Xcode:**

1. Go to **File > Add Package Dependencies**
2. Enter the repository URL:
   ```
   https://github.com/SparrowDesk/sparrowdesk-mobile-sdk
   ```
3. Select version **0.1.1** or later

**In `Package.swift`:**

```swift
dependencies: [
    .package(url: "https://github.com/SparrowDesk/sparrowdesk-mobile-sdk", from: "0.1.1")
]
```

## Quick Start

### Android (Kotlin)

```kotlin
import com.sparrowdesk.sdk.SparrowDeskConfig
import com.sparrowdesk.sdk.SparrowDeskSDK

class MainActivity : AppCompatActivity() {

    private lateinit var sdk: SparrowDeskSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Create the SDK
        sdk = SparrowDeskSDK(SparrowDeskConfig(
            domain = "yourcompany.sparrowdesk.com",
            token  = "your-widget-token"
        ))

        // 2. Identify the user (can be called before or after attach)
        sdk.setContactFields(mapOf(
            "email" to "user@example.com",
            "name"  to "Jane Doe"
        ))

        // 3. Attach the WebView to a container in your layout
        val container: FrameLayout = findViewById(R.id.webview_container)
        sdk.attach(this, container)

        // 4. Open the chat when a button is tapped
        findViewById<Button>(R.id.btn_chat).setOnClickListener {
            sdk.openWidget()
        }
    }

    override fun onDestroy() {
        sdk.destroy()
        super.onDestroy()
    }
}
```

### iOS (Swift)

```swift
import SparrowDeskSDK

class ChatViewController: UIViewController {

    private var sdk: SparrowDeskSDK!

    override func viewDidLoad() {
        super.viewDidLoad()

        // 1. Create the SDK
        let config = SparrowDeskConfig(domain: "yourcompany.sparrowdesk.com",
                                       token:  "your-widget-token")
        sdk = SparrowDeskSDK(config: config)

        // 2. Identify the user
        sdk.setContactFields(fields: ["email": "user@example.com",
                                       "name": "Jane Doe"])

        // 3. Attach to a view
        sdk.attach(parentView: view)

        // 4. Open the chat
        sdk.openWidget()
    }

    deinit {
        sdk.destroy()
    }
}
```

> **Note (iOS):** Kotlin `fun interface` types export as ObjC protocols. Swift closures
> don't conform automatically, so wrap them when using `onOpen` / `onClose`:
>
> ```swift
> class CallbackWrapper: NSObject, SparrowDeskCallback {
>     private let handler: () -> Void
>     init(_ handler: @escaping () -> Void) { self.handler = handler }
>     func onEvent() { handler() }
> }
>
> sdk.onOpen(callback: CallbackWrapper { print("Widget opened") })
> ```

## API Reference

### `SparrowDeskConfig`

```kotlin
data class SparrowDeskConfig(
    val domain: String,              // e.g. "yourcompany.sparrowdesk.com"
    val token: String,               // Your widget token from the SparrowDesk dashboard
    val debug: Boolean = false       // When true, the SDK emits diagnostic logs
                                     // (Android: logcat tag "SparrowDeskSDK"; iOS: Xcode console)
)
```

> Set `debug = true` during integration to trace the load / ready / open / close
> lifecycle. Leave it `false` (default) in release builds — gated calls are no-ops.

### `SparrowDeskSDK`

| Method | Description |
|--------|-------------|
| `attach(context, parent)` | **Android.** Creates the WebView and adds it to the given `ViewGroup`. |
| `attach(parentView)` | **iOS.** Creates the `WKWebView` and adds it to the given `UIView`. |
| `openWidget()` | Opens the chat widget. |
| `closeWidget()` | Closes the chat widget. |
| `hideWidget()` | Hides both the launcher button and the widget panel. |
| `onOpen(callback)` | Registers a callback that fires when the widget opens. |
| `onClose(callback)` | Registers a callback that fires when the widget closes. |
| `setTags(tags)` | Attaches string tags to the current session. |
| `setContactFields(fields)` | Sets contact fields (e.g. `email`, `name`, `phone`). |
| `setConversationFields(fields)` | Sets conversation-level custom fields. |
| `getStatus(callback)` | Queries the widget state asynchronously (`OPEN`, `CLOSED`, `UNKNOWN`). |
| `show()` | Makes the WebView container visible. |
| `hide()` | Hides the WebView container. |
| `destroy()` | Tears down the WebView and releases all resources. |

### `WidgetStatus`

```kotlin
enum class WidgetStatus { OPEN, CLOSED, UNKNOWN }
```

## Usage Examples

### Set User Identity

Call before or after `attach()` — commands are queued and replayed once the widget loads.

```kotlin
sdk.setContactFields(mapOf(
    "email" to "user@example.com",
    "name"  to "Jane Doe",
    "phone" to "+1234567890"
))
```

### Attach Session Tags

```kotlin
sdk.setTags(listOf("vip", "enterprise", "priority-support"))
```

### Set Conversation Fields

```kotlin
sdk.setConversationFields(mapOf(
    "plan"    to "enterprise",
    "source"  to "mobile-app"
))
```

### Listen to Widget Events

```kotlin
sdk.onOpen  { Log.d("SparrowDesk", "Chat widget opened") }
sdk.onClose { Log.d("SparrowDesk", "Chat widget closed") }
```

### Query Widget Status

```kotlin
sdk.getStatus { status ->
    when (status) {
        WidgetStatus.OPEN   -> Log.d("SparrowDesk", "Widget is open")
        WidgetStatus.CLOSED -> Log.d("SparrowDesk", "Widget is closed")
        WidgetStatus.UNKNOWN -> Log.d("SparrowDesk", "Widget not ready yet")
    }
}
```

### Toggle Visibility Programmatically

```kotlin
// Hide the entire WebView (e.g. during onboarding)
sdk.hide()

// Show it again
sdk.show()
```

### Clean Up

Always call `destroy()` when the hosting Activity/ViewController is torn down:

```kotlin
override fun onDestroy() {
    sdk.destroy()
    super.onDestroy()
}
```

### Debug Logging

Set `debug = true` on `SparrowDeskConfig` to emit diagnostic logs covering the
load / ready / open / close lifecycle, native ↔ JS bridge traffic, WebView
errors, and the pending-command queue.

```kotlin
val sdk = SparrowDeskSDK(SparrowDeskConfig(
    domain = "yourcompany.sparrowdesk.com",
    token  = "your-widget-token",
    debug  = true
))
```

- **Android:** `adb logcat -s SparrowDeskSDK:V` (logs use `android.util.Log`
  under the tag `SparrowDeskSDK`).
- **iOS:** logs are emitted via `print()` and appear in the Xcode console.

Leave `debug` at its default (`false`) for release builds — all logging calls
are gated and compile down to cheap no-ops.

## Architecture

```
┌─────────────────────────────────────────────────┐
│                   Your App                       │
│                                                  │
│   SparrowDeskSDK(config)                         │
│       .attach(context, viewGroup)  // Android    │
│       .attach(parentView)          // iOS        │
│       .openWidget()                              │
│       .setContactFields(...)                     │
└──────────────────┬──────────────────────────────┘
                   │  evaluateJavascript()
                   ▼
┌─────────────────────────────────────────────────┐
│              WebView (platform)                  │
│                                                  │
│   window.SD_WIDGET_TOKEN = "..."                 │
│   window.SD_WIDGET_DOMAIN = "..."                │
│   <script src="assets.cdn.sparrowdesk.com/...">  │
│                                                  │
│   window.sparrowDesk.openWidget()                │
│   window.sparrowDesk.setContactFields({...})     │
└──────────────────┬──────────────────────────────┘
                   │  NativeBridge.postMessage()
                   ▼
┌─────────────────────────────────────────────────┐
│           Native Bridge (per-platform)           │
│                                                  │
│   Android: @JavascriptInterface                  │
│   iOS:     WKScriptMessageHandler                │
│                                                  │
│   Events: "widgetReady", "onOpen", "onClose"     │
└─────────────────────────────────────────────────┘
```

**Command queuing:** Methods like `setContactFields()` and `setTags()` can be called before the widget has loaded. Commands are queued internally and replayed automatically once the widget signals `"widgetReady"`.

## Project Structure

```
sparrowdesk-mobile-sdk/
├── sparrowdesk-sdk/                       # The SDK library
│   └── src/
│       ├── commonMain/.../sdk/
│       │   ├── SparrowDeskSDK.kt          # expect class — public API
│       │   ├── SparrowDeskConfig.kt       # Configuration data class
│       │   ├── SparrowDeskCallback.kt     # Event callback interface
│       │   ├── WidgetStatus.kt            # Widget state enum
│       │   ├── HtmlTemplate.kt            # HTML + script injection
│       │   └── JsBridge.kt               # JS eval string builders
│       ├── androidMain/.../sdk/
│       │   └── SparrowDeskSDK.android.kt  # Android WebView implementation
│       ├── iosMain/.../sdk/
│       │   └── SparrowDeskSDK.ios.kt      # iOS WKWebView implementation
│       └── commonTest/.../sdk/
│           ├── JsBridgeTest.kt            # JS bridge unit tests
│           ├── HtmlTemplateTest.kt        # HTML template tests
│           └── WidgetStatusTest.kt        # Enum parsing tests
│
├── sample/
│   ├── androidApp/                        # Android sample app
│   ├── iosApp/                            # iOS SwiftUI sample app
│   └── README.md                          # Sample app setup instructions
│
├── Package.swift                          # Swift Package Manager manifest
├── scripts/publish.sh                     # Release automation script
└── .github/workflows/publish.yml          # CI/CD publish workflow
```

## Building from Source

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| JDK  | 17+     | Kotlin compilation |
| Android SDK | API 35 | Android target |
| Xcode | 15+ | iOS framework linking |

### Build

```bash
# All iOS targets (compile only — no Xcode needed)
./gradlew :sparrowdesk-sdk:compileKotlinIosArm64 \
          :sparrowdesk-sdk:compileKotlinIosSimulatorArm64 \
          :sparrowdesk-sdk:compileKotlinIosX64

# Android AAR
./gradlew :sparrowdesk-sdk:bundleReleaseAar

# XCFramework (requires Xcode)
./gradlew :sparrowdesk-sdk:assembleSparrowDeskSDKXCFramework

# Run unit tests
./gradlew :sparrowdesk-sdk:iosSimulatorArm64Test
```

### Sample Apps

```bash
# Android — build and install
./gradlew :sample:androidApp:assembleDebug
adb install sample/androidApp/build/outputs/apk/debug/androidApp-debug.apk

# iOS — build framework then open Xcode
./gradlew :sparrowdesk-sdk:linkReleaseFrameworkIosSimulatorArm64
open sample/iosApp/SparrowDeskSample.xcodeproj
```

The Android sample demonstrates both embedded (half-screen) and fullscreen
presentation modes, per-session contact fields and tags, and every public
control method (`openWidget`, `closeWidget`, `hideWidget`, `show`, `hide`,
`getStatus`, `destroy`). In fullscreen mode the widget's own close icon or the
system back button both route through `onClose` to restore the host layout —
use it as a reference when wiring a fullscreen chat experience.

## Publishing

### Automated (GitHub Actions)

Go to **Actions > Publish SDK > Run workflow** and enter the version (e.g. `0.2.0`). The workflow:

1. Builds the XCFramework and uploads it to a GitHub Release (for Swift Package Manager)
2. Updates `Package.swift` with the new checksum and commits it
3. Tags the commit — JitPack builds the Android AAR on-demand from the tag

> **Android distribution:** The Android AAR is served by [JitPack](https://jitpack.io)
> directly from git tags. No publish step is required — the first time a
> consumer requests `com.github.SparrowDesk.sparrowdesk-mobile-sdk:sparrowdesk-sdk:<tag>`,
> JitPack clones the tag, runs the build defined in `jitpack.yml`, and caches
> the result.

### Manual

```bash
export GITHUB_TOKEN="ghp_..."   # needs `contents: write` scope for the GitHub Release
./scripts/publish.sh 0.2.0
```

## JavaScript API Mapping

The SDK maps to the [SparrowDesk JavaScript API](https://developer.sparrowdesk.com/chat-widget/javascript-api):

| JS API | SDK Method |
|--------|-----------|
| `sparrowDesk.openWidget()` | `sdk.openWidget()` |
| `sparrowDesk.closeWidget()` | `sdk.closeWidget()` |
| `sparrowDesk.hideWidget()` | `sdk.hideWidget()` |
| `sparrowDesk.onOpen(cb)` | `sdk.onOpen(callback)` |
| `sparrowDesk.onClose(cb)` | `sdk.onClose(callback)` |
| `sparrowDesk.setTags([...])` | `sdk.setTags(listOf(...))` |
| `sparrowDesk.setContactFields({...})` | `sdk.setContactFields(mapOf(...))` |
| `sparrowDesk.setConversationFields({...})` | `sdk.setConversationFields(mapOf(...))` |
| `sparrowDesk.status` | `sdk.getStatus { status -> }` |

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
