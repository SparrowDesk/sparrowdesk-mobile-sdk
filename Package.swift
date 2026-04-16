// swift-tools-version:5.9

import PackageDescription

// ┌────────────────────────────────────────────────────────────┐
// │  UPDATE THESE on every release                             │
// │  The publish script (scripts/publish.sh) patches them      │
// │  automatically from Gradle outputs.                        │
// └────────────────────────────────────────────────────────────┘
let version = "0.1.0"
let checksum = "CHECKSUM_PLACEHOLDER"
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
