package com.sparrowdesk.sdk

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class SparrowDeskSDK actual constructor(private val config: SparrowDeskConfig) {

    private var webView: WKWebView? = null
    private var userContentController: WKUserContentController? = null
    private var messageHandler: MessageHandler? = null
    private var isWidgetReady = false
    private val pendingCommands = mutableListOf<String>()

    private var onOpenCallback: SparrowDeskCallback? = null
    private var onCloseCallback: SparrowDeskCallback? = null

    /**
     * Creates the WKWebView and attaches it to the given parent UIView.
     * Must be called from the main thread before using any widget methods.
     *
     * @param parentView The UIView to which the WKWebView will be added
     */
    fun attach(parentView: UIView) {
        if (webView != null) return // Already attached

        val contentController = WKUserContentController()
        userContentController = contentController

        // Inject the NativeBridge adapter script at document start
        val bridgeScript = WKUserScript(
            source = """
                window.NativeBridge = {
                    postMessage: function(msg) {
                        window.webkit.messageHandlers.NativeBridge.postMessage(msg);
                    }
                };
            """.trimIndent(),
            injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
            forMainFrameOnly = true
        )
        contentController.addUserScript(bridgeScript)

        // Register the native message handler
        val handler = MessageHandler()
        messageHandler = handler
        contentController.addScriptMessageHandler(handler, "NativeBridge")

        val configuration = WKWebViewConfiguration().apply {
            this.userContentController = contentController
        }

        val wv = WKWebView(
            frame = parentView.bounds,
            configuration = configuration
        ).apply {
            setAutoresizingMask(
                UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
            )
            setOpaque(false)
        }

        webView = wv
        parentView.addSubview(wv)

        // Load the HTML template
        val html = HtmlTemplate.build(config)
        wv.loadHTMLString(html, baseURL = NSURL(string = "https://${config.domain}"))
    }

    actual fun openWidget() {
        runOrQueue(JsBridge.openWidget())
    }

    actual fun closeWidget() {
        runOrQueue(JsBridge.closeWidget())
    }

    actual fun hideWidget() {
        runOrQueue(JsBridge.hideWidget())
    }

    actual fun onOpen(callback: SparrowDeskCallback) {
        onOpenCallback = callback
    }

    actual fun onClose(callback: SparrowDeskCallback) {
        onCloseCallback = callback
    }

    actual fun setTags(tags: List<String>) {
        runOrQueue(JsBridge.setTags(tags))
    }

    actual fun setConversationFields(fields: Map<String, String>) {
        runOrQueue(JsBridge.setConversationFields(fields))
    }

    actual fun setContactFields(fields: Map<String, String>) {
        runOrQueue(JsBridge.setContactFields(fields))
    }

    actual fun getStatus(callback: (WidgetStatus) -> Unit) {
        val wv = webView ?: run {
            callback(WidgetStatus.UNKNOWN)
            return
        }
        wv.evaluateJavaScript(JsBridge.getStatus()) { result, _ ->
            val status = WidgetStatus.fromString(
                (result as? String) ?: ""
            )
            callback(status)
        }
    }

    actual fun show() {
        webView?.setHidden(false)
    }

    actual fun hide() {
        webView?.setHidden(true)
    }

    actual fun destroy() {
        // Remove script message handler to break retain cycle
        userContentController?.removeScriptMessageHandlerForName("NativeBridge")

        webView?.stopLoading()
        webView?.removeFromSuperview()
        webView = null
        userContentController = null
        messageHandler = null
        isWidgetReady = false
        pendingCommands.clear()
        onOpenCallback = null
        onCloseCallback = null
    }

    // --- Private helpers ---

    private fun runOrQueue(js: String) {
        if (isWidgetReady) {
            executeJs(js)
        } else {
            pendingCommands.add(js)
        }
    }

    private fun executeJs(js: String) {
        webView?.evaluateJavaScript(js, completionHandler = null)
    }

    private fun flushPendingCommands() {
        pendingCommands.forEach { executeJs(it) }
        pendingCommands.clear()
    }

    /**
     * Handles messages posted from JavaScript via window.NativeBridge.postMessage().
     */
    private inner class MessageHandler : NSObject(), WKScriptMessageHandlerProtocol {
        override fun userContentController(
            userContentController: WKUserContentController,
            didReceiveScriptMessage: WKScriptMessage
        ) {
            val message = didReceiveScriptMessage.body as? String ?: return
            when (message) {
                "widgetReady" -> {
                    isWidgetReady = true
                    flushPendingCommands()
                }
                "onOpen" -> onOpenCallback?.onEvent()
                "onClose" -> onCloseCallback?.onEvent()
            }
        }
    }
}
