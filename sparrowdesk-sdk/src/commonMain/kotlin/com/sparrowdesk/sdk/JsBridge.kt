package com.sparrowdesk.sdk

/**
 * Pure functions that produce JavaScript strings for evaluating in the WebView.
 * All user-provided values are escaped to prevent injection.
 */
internal object JsBridge {

    fun openWidget(): String = "window.SparrowDesk.openWidget();"

    fun closeWidget(): String = "window.SparrowDesk.closeWidget();"

    fun hideWidget(): String = "window.SparrowDesk.hideWidget();"

    fun getStatus(): String = "window.SparrowDesk.status"

    fun setTags(tags: List<String>): String {
        val escaped = tags.joinToString(",") { "\"${escapeJs(it)}\"" }
        return "window.SparrowDesk.setTags([$escaped]);"
    }

    fun setConversationFields(fields: Map<String, String>): String {
        return "window.SparrowDesk.setConversationFields(${mapToJsObject(fields)});"
    }

    fun setContactFields(fields: Map<String, String>): String {
        return "window.SparrowDesk.setContactFields(${mapToJsObject(fields)});"
    }

    private fun mapToJsObject(map: Map<String, String>): String {
        val entries = map.entries.joinToString(",") { (key, value) ->
            "\"${escapeJs(key)}\":\"${escapeJs(value)}\""
        }
        return "{$entries}"
    }

    /**
     * Escapes special characters in a string for safe JavaScript string interpolation.
     */
    internal fun escapeJs(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("<", "\\x3c")  // Prevent </script> injection
            .replace(">", "\\x3e")
    }
}
