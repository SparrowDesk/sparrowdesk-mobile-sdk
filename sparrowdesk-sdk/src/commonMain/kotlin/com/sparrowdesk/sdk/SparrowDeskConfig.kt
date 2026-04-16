package com.sparrowdesk.sdk

/**
 * Configuration for initializing the SparrowDesk SDK.
 *
 * @param domain Your SparrowDesk domain (e.g., "yourcompany.sparrowdesk.com")
 * @param token  Your widget token
 */
data class SparrowDeskConfig(
    val domain: String,
    val token: String
)
