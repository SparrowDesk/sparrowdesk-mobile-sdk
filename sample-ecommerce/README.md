# SparrowDesk Store — E-Commerce Sample App

A simple e-commerce demo app (Android + iOS) that showcases how to integrate the **SparrowDesk Mobile SDK** into a real-world app. Use this to record a walkthrough of the SDK in action.

## Screens

| Screen | Description |
|--------|-------------|
| **Product Catalog** | 2×2 grid of products with a "Chat with Support" floating button |
| **Product Detail** | Product info, "Add to Cart", and a support chat link |
| **Shopping Cart** | Cart items, subtotal, checkout, and support chat link |
| **Support Chat** | Full-screen SparrowDesk chat widget (SDK integration) |

---

## Setup

### 1. Configure Your Credentials

You need your **Domain** and **Widget Token** from the SparrowDesk dashboard:

1. Log in to your SparrowDesk dashboard
2. Go to **Settings → Chat Widget**
3. Copy your **Domain** and **Widget Token**

#### Android

Edit `androidApp/src/main/java/com/sparrowdesk/ecommerce/AppConfig.kt`:

```kotlin
object AppConfig {
    const val SPARROWDESK_DOMAIN = "yourcompany.sparrowdesk.com"   // ← your domain
    const val SPARROWDESK_TOKEN  = "your-widget-token-here"        // ← your token
}
```

#### iOS

Edit `iosApp/SparrowDeskStore/AppConfig.swift`:

```swift
enum AppConfig {
    static let sparrowDeskDomain = "yourcompany.sparrowdesk.com"   // ← your domain
    static let sparrowDeskToken  = "your-widget-token-here"        // ← your token
}
```

---

### 2. Build & Run

#### Android

**Prerequisites:** Android Studio, JDK 17+

```bash
# From the repository root
./gradlew :sample-ecommerce:androidApp:installDebug
```

Or open the project root in Android Studio and run the `sample-ecommerce:androidApp` configuration.

#### iOS

**Prerequisites:** Xcode 15+, macOS

1. Build the SDK framework first (from repository root):
   ```bash
   ./gradlew :sparrowdesk-sdk:linkReleaseFrameworkIosSimulatorArm64
   ```

2. Open the Xcode project:
   ```bash
   open sample-ecommerce/iosApp/SparrowDeskStore.xcodeproj
   ```

3. Select a simulator and press **Run** (⌘R).

> **Note:** The Xcode project includes a build phase that automatically runs the Gradle framework build. If you've already built the framework, subsequent builds will be faster.

---

## App Flow (for screen recording)

1. **Launch** → Product catalog with 4 items
2. **Tap a product** → See product details, description, and pricing
3. **Add to Cart** → Toast confirmation, cart badge updates
4. **Open Cart** (toolbar icon) → Review items, see subtotal
5. **Tap "Chat with Support"** (FAB or link) → Full-screen SparrowDesk chat widget loads
6. **Chat** → Send a message to demonstrate live support
7. **Navigate back** → Return to shopping flow

---

## Project Structure

```
sample-ecommerce/
├── README.md
├── androidApp/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/sparrowdesk/ecommerce/
│       │   ├── AppConfig.kt              ← Configure domain & token here
│       │   ├── Product.kt                ← Product catalog model
│       │   ├── CartManager.kt            ← Cart state management
│       │   ├── MainActivity.kt           ← Product grid screen
│       │   ├── ProductDetailActivity.kt  ← Product detail screen
│       │   ├── CartActivity.kt           ← Shopping cart screen
│       │   └── SupportChatActivity.kt    ← SDK chat integration
│       └── res/
│           ├── layout/                    ← XML layouts for all screens
│           ├── drawable/                  ← Vector icons (cart, chat, back)
│           ├── menu/                      ← Toolbar menu (cart icon)
│           └── values/                    ← Colors, strings, themes
│
└── iosApp/
    ├── SparrowDeskStore.xcodeproj/
    └── SparrowDeskStore/
        ├── SparrowDeskStoreApp.swift      ← App entry point
        ├── AppConfig.swift                ← Configure domain & token here
        ├── Product.swift                  ← Product catalog model
        ├── CartManager.swift              ← Cart state management
        ├── ProductListView.swift          ← Product grid screen
        ├── ProductDetailView.swift        ← Product detail screen
        ├── CartView.swift                 ← Shopping cart screen
        ├── SupportChatView.swift          ← SDK chat integration
        ├── Info.plist
        └── Assets.xcassets/
```

---

## SDK Integration Highlights

The key integration point is in `SupportChatActivity.kt` (Android) and `SupportChatView.swift` (iOS):

```kotlin
// Android — SupportChatActivity.kt
val config = SparrowDeskConfig(
    domain = AppConfig.SPARROWDESK_DOMAIN,
    token = AppConfig.SPARROWDESK_TOKEN
)
val sdk = SparrowDeskSDK(config)
sdk.attach(this, container)
sdk.openWidget()
sdk.hideWidget()   // hides the launcher bubble, shows only the chat panel
```

```swift
// iOS — SupportChatView.swift
let config = SparrowDeskConfig(
    domain: AppConfig.sparrowDeskDomain,
    token: AppConfig.sparrowDeskToken
)
let sdk = SparrowDeskSDK(config: config)
sdk.attach(parentView: container)
sdk.openWidget()
sdk.hideWidget()   // hides the launcher bubble, shows only the chat panel
```

The `hideWidget()` call after `openWidget()` hides the floating launcher button so only the chat conversation panel is visible — ideal for a full-screen support chat experience.
