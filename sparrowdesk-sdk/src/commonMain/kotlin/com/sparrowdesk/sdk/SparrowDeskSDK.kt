package com.sparrowdesk.sdk

/**
 * SparrowDesk Chat Widget SDK for mobile platforms.
 *
 * Usage:
 * ```
 * val sdk = SparrowDeskSDK(SparrowDeskConfig(
 *     domain = "yourcompany.sparrowdesk.com",
 *     token = "your-widget-token"
 * ))
 *
 * // Android: sdk.attach(context, parentViewGroup)
 * // iOS:     sdk.attach(parentUIView)
 *
 * sdk.setContactFields(mapOf("email" to "user@example.com"))
 * sdk.openWidget()
 * ```
 */
expect class SparrowDeskSDK(config: SparrowDeskConfig) {

    /** Opens the chat widget. */
    fun openWidget()

    /** Closes the chat widget. */
    fun closeWidget()

    /** Hides both the launcher button and the widget panel. */
    fun hideWidget()

    /** Registers a callback that fires when the widget opens. */
    fun onOpen(callback: SparrowDeskCallback)

    /** Registers a callback that fires when the widget closes. */
    fun onClose(callback: SparrowDeskCallback)

    /** Attaches tags to the current chat session. */
    fun setTags(tags: List<String>)

    /** Sets conversation field values (use internal field names). */
    fun setConversationFields(fields: Map<String, String>)

    /** Sets contact fields for user identification (email, name, etc.). */
    fun setContactFields(fields: Map<String, String>)

    /** Queries the current widget status asynchronously. */
    fun getStatus(callback: (WidgetStatus) -> Unit)

    /** Shows the WebView container (makes it visible). */
    fun show()

    /** Hides the WebView container (makes it invisible). */
    fun hide()

    /** Destroys the WebView and cleans up all resources. */
    fun destroy()
}
