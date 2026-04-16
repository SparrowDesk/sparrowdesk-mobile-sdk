package com.sparrowdesk.sdk

/**
 * Represents the current state of the chat widget.
 */
enum class WidgetStatus {
    OPEN,
    CLOSED,
    UNKNOWN;

    companion object {
        fun fromString(value: String): WidgetStatus = when (value.lowercase()) {
            "open" -> OPEN
            "closed" -> CLOSED
            else -> UNKNOWN
        }
    }
}
