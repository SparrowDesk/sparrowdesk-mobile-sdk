# SparrowDesk SDK — Sample Apps

Minimal sample apps demonstrating how to integrate the SparrowDesk Chat Widget SDK.

## Android Sample

### Prerequisites
- Android Studio (Arctic Fox or later)
- Android SDK 35
- Java 17

### Run from Android Studio
1. Open the **root project** (`sparrowdesk-mobile-sdk/`) in Android Studio
2. Select the `sample:androidApp` run configuration
3. Choose an emulator or connected device (API 24+)
4. Click **Run**

### Run from command line
```bash
# Build the debug APK
./gradlew :sample:androidApp:assembleDebug

# Install on a connected device/emulator
adb install sample/androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### What the sample does
- Enter your SparrowDesk **domain** and **token** (defaults are pre-filled)
- Optionally set user **email**, **name**, and **tags**
- Tap **Load Widget** to initialize the SDK and attach the WebView
- Use the control buttons to test all SDK methods:
  - **Open / Close** — opens/closes the chat widget
  - **Hide Widget** — hides the widget launcher and panel
  - **Show / Hide WebView** — toggles the WebView container visibility
  - **Status** — queries the current widget state
  - **Destroy** — cleans up the SDK and WebView

---

## iOS Sample

### Prerequisites
- Xcode 15+ (with iOS 16+ SDK)
- macOS 13+

### Setup
1. First, build the iOS framework from the root project:
   ```bash
   ./gradlew :sparrowdesk-sdk:linkReleaseFrameworkIosSimulatorArm64
   ```
2. Open `sample/iosApp/SparrowDeskSample.xcodeproj` in Xcode
3. The project has a **Build Phase** script that automatically runs the Gradle
   framework build, but running it manually first saves time
4. Select an iOS Simulator and click **Run**

### What the sample does
Same functionality as the Android sample — config form, control buttons, event log.

---

## SDK Usage Example

### Android (Kotlin)
```kotlin
val sdk = SparrowDeskSDK(SparrowDeskConfig(
    domain = "yourcompany.sparrowdesk.com",
    token = "your-widget-token"
))

// Set user info before or after loading
sdk.setContactFields(mapOf("email" to "user@example.com", "name" to "John"))
sdk.setTags(listOf("vip", "premium"))

// Attach to a ViewGroup and load
sdk.attach(context, parentViewGroup)

// Control the widget
sdk.openWidget()
sdk.closeWidget()
sdk.hideWidget()

// Listen to events
sdk.onOpen { Log.d("Widget", "Opened!") }
sdk.onClose { Log.d("Widget", "Closed!") }

// Query status
sdk.getStatus { status -> Log.d("Widget", "Status: $status") }

// Show/hide the entire WebView
sdk.show()
sdk.hide()

// Clean up
sdk.destroy()
```

### iOS (Swift)
```swift
import SparrowDeskSDK

let config = SparrowDeskConfig(domain: "yourcompany.sparrowdesk.com",
                                token: "your-widget-token")
let sdk = SparrowDeskSDK(config: config)

sdk.setContactFields(fields: ["email": "user@example.com", "name": "John"])
sdk.setTags(tags: ["vip", "premium"])

sdk.attach(parentView: containerView)

sdk.openWidget()
sdk.onOpen { print("Widget opened!") }
sdk.onClose { print("Widget closed!") }
sdk.getStatus { status in print("Status: \(status)") }

sdk.destroy()
```
