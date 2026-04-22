package com.sparrowdesk.sdk

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
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
    private var navigationDelegate: NavigationDelegate? = null
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
        if (webView != null) {
            logd("attach: already attached, ignoring")
            return
        }
        logd("attach: domain=${config.domain}")

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

        val navDelegate = NavigationDelegate()
        navigationDelegate = navDelegate

        val wv = WKWebView(
            frame = parentView.bounds,
            configuration = configuration
        ).apply {
            setAutoresizingMask(
                UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
            )
            setOpaque(false)
            setNavigationDelegate(navDelegate)
        }

        webView = wv
        parentView.addSubview(wv)

        // Load the HTML template
        val html = HtmlTemplate.build(config)
        wv.loadHTMLString(html, baseURL = NSURL(string = "https://${config.domain}"))
        logd("attach: HTML loaded")
    }

    actual fun openWidget() {
        logd("openWidget()")
        runOrQueue(JsBridge.openWidget())
    }

    actual fun closeWidget() {
        logd("closeWidget()")
        runOrQueue(JsBridge.closeWidget())
    }

    actual fun hideWidget() {
        logd("hideWidget()")
        runOrQueue(JsBridge.hideWidget())
    }

    actual fun onOpen(callback: SparrowDeskCallback) {
        logd("onOpen: callback registered")
        onOpenCallback = callback
    }

    actual fun onClose(callback: SparrowDeskCallback) {
        logd("onClose: callback registered")
        onCloseCallback = callback
    }

    actual fun setTags(tags: List<String>) {
        logd("setTags: $tags")
        runOrQueue(JsBridge.setTags(tags))
    }

    actual fun setConversationFields(fields: Map<String, String>) {
        logd("setConversationFields: keys=${fields.keys}")
        runOrQueue(JsBridge.setConversationFields(fields))
    }

    actual fun setContactFields(fields: Map<String, String>) {
        logd("setContactFields: keys=${fields.keys}")
        runOrQueue(JsBridge.setContactFields(fields))
    }

    actual fun getStatus(callback: (WidgetStatus) -> Unit) {
        logd("getStatus()")
        val wv = webView ?: run {
            logw("getStatus: WKWebView not attached, returning UNKNOWN")
            callback(WidgetStatus.UNKNOWN)
            return
        }
        wv.evaluateJavaScript(JsBridge.getStatus()) { result, _ ->
            val raw = (result as? String) ?: ""
            logd("getStatus result=$raw")
            callback(WidgetStatus.fromString(raw))
        }
    }

    actual fun show() {
        logd("show()")
        webView?.setHidden(false)
    }

    actual fun hide() {
        logd("hide()")
        webView?.setHidden(true)
    }

    actual fun destroy() {
        logd("destroy()")
        // Remove script message handler to break retain cycle
        userContentController?.removeScriptMessageHandlerForName("NativeBridge")

        webView?.setNavigationDelegate(null)
        webView?.stopLoading()
        webView?.removeFromSuperview()
        webView = null
        userContentController = null
        messageHandler = null
        navigationDelegate = null
        isWidgetReady = false
        pendingCommands.clear()
        onOpenCallback = null
        onCloseCallback = null
    }

    // --- Private helpers ---

    private fun runOrQueue(js: String) {
        if (isWidgetReady) {
            logd("runOrQueue: executing (widget ready)")
            executeJs(js)
        } else {
            pendingCommands.add(js)
            logd("runOrQueue: queued (pending=${pendingCommands.size})")
        }
    }

    private fun executeJs(js: String) {
        val wv = webView ?: run {
            logw("executeJs: WKWebView is null, dropping command")
            return
        }
        wv.evaluateJavaScript(js, completionHandler = null)
    }

    private fun flushPendingCommands() {
        logd("flushPendingCommands: count=${pendingCommands.size}")
        pendingCommands.forEach { executeJs(it) }
        pendingCommands.clear()
    }

    private fun logd(message: String) {
        if (config.debug) SDKLogger.d(TAG, message)
    }

    private fun logw(message: String) {
        if (config.debug) SDKLogger.w(TAG, message)
    }

    private fun loge(message: String) {
        if (config.debug) SDKLogger.e(TAG, message)
    }

    /**
     * Handles messages posted from JavaScript via window.NativeBridge.postMessage().
     */
    private inner class MessageHandler : NSObject(), WKScriptMessageHandlerProtocol {
        override fun userContentController(
            userContentController: WKUserContentController,
            didReceiveScriptMessage: WKScriptMessage
        ) {
            val message = didReceiveScriptMessage.body as? String ?: run {
                logw("bridge: non-string message body")
                return
            }
            when (message) {
                "widgetReady" -> {
                    logd("bridge: widgetReady")
                    isWidgetReady = true
                    flushPendingCommands()
                }
                "onOpen" -> {
                    logd("bridge: onOpen")
                    onOpenCallback?.onEvent()
                }
                "onClose" -> {
                    logd("bridge: onClose")
                    onCloseCallback?.onEvent()
                }
                else -> logw("bridge: unknown message '$message'")
            }
        }
    }

    /**
     * Surfaces WKWebView navigation failures so JS/network errors are visible.
     */
    private inner class NavigationDelegate : NSObject(), WKNavigationDelegateProtocol {
        override fun webView(
            webView: WKWebView,
            didFailProvisionalNavigation: WKNavigation?,
            withError: NSError
        ) {
            loge("WKWebView didFailProvisionalNavigation: ${withError.localizedDescription}")
        }
    }

    private companion object {
        const val TAG = "SparrowDeskSDK"
    }
}
