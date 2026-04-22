package com.sparrowdesk.sdk

/**
 * Configuration for initializing the SparrowDesk SDK.
 *
 * @param domain Your SparrowDesk domain (e.g., "yourcompany.sparrowdesk.com")
 * @param token  Your widget token
 * @param debug  When true, the SDK emits diagnostic logs (Android: logcat tag
 *               "SparrowDeskSDK"; iOS: Xcode console). Leave false in release builds.
 */
data class SparrowDeskConfig(
    val domain: String,
    val token: String,
    val debug: Boolean = false
)
