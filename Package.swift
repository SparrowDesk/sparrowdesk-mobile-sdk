// swift-tools-version:5.9

import PackageDescription

// ┌────────────────────────────────────────────────────────────┐
// │  UPDATE THESE on every release                             │
// │  The publish script (scripts/publish.sh) patches them      │
// │  automatically from Gradle outputs.                        │
// └────────────────────────────────────────────────────────────┘
let version = "0.1.0"
let checksum = "fc2b9953217415ce6e3ed4e65f1485735f400499248edb03f1e91b585a3b9370"
let url = "https://github.com/SparrowDesk/sparrowdesk-mobile-sdk/releases/download/v\(version)/SparrowDeskSDK.xcframework.zip"

let package = Package(
    name: "SparrowDeskSDK",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "SparrowDeskSDK",
            targets: ["SparrowDeskSDK"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "SparrowDeskSDK",
            url: url,
            checksum: checksum
        )
    ]
)
