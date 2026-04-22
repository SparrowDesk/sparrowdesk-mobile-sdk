package com.sparrowdesk.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

actual class SparrowDeskSDK actual constructor(private val config: SparrowDeskConfig) {

    private var webView: WebView? = null
    private var isWidgetReady = false
    private val pendingCommands = mutableListOf<String>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var onOpenCallback: SparrowDeskCallback? = null
    private var onCloseCallback: SparrowDeskCallback? = null

    /**
     * Creates the WebView and attaches it to the given parent ViewGroup.
     * Must be called from the main thread before using any widget methods.
     *
     * @param context Android Context (Activity or Application)
     * @param parent  The ViewGroup to which the WebView will be added
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun attach(context: Context, parent: ViewGroup) {
        if (webView != null) {
            logd("attach: already attached, ignoring")
            return
        }
        logd("attach: domain=${config.domain}")

        val wv = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    loge(
                        "WebView onReceivedError: url=${request?.url} " +
                            "code=${error?.errorCode} desc=${error?.description}"
                    )
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        logd(
                            "JS console [${it.messageLevel()}] " +
                                "${it.sourceId()}:${it.lineNumber()} ${it.message()}"
                        )
                    }
                    return true
                }
            }

            // Register the native bridge
            addJavascriptInterface(NativeBridgeInterface(), "NativeBridgeAndroid")
        }

        webView = wv
        parent.addView(wv)

        // Build HTML and inject the platform-specific NativeBridge adapter
        val html = HtmlTemplate.build(config)
        val htmlWithBridge = html.replace(
            "<script>",
            """<script>
window.NativeBridge = {
    postMessage: function(msg) {
        NativeBridgeAndroid.postMessage(msg);
    }
};
""",
            // Replace only the first occurrence
        )

        wv.loadDataWithBaseURL(
            "https://${config.domain}",
            htmlWithBridge,
            "text/html",
            "UTF-8",
            null
        )
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
            logw("getStatus: WebView not attached, returning UNKNOWN")
            callback(WidgetStatus.UNKNOWN)
            return
        }
        mainHandler.post {
            wv.evaluateJavascript(JsBridge.getStatus()) { result ->
                logd("getStatus result=$result")
                val status = WidgetStatus.fromString(
                    result?.removeSurrounding("\"") ?: ""
                )
                callback(status)
            }
        }
    }

    actual fun show() {
        logd("show()")
        mainHandler.post { webView?.visibility = View.VISIBLE }
    }

    actual fun hide() {
        logd("hide()")
        mainHandler.post { webView?.visibility = View.GONE }
    }

    actual fun destroy() {
        logd("destroy()")
        mainHandler.post {
            webView?.let { wv ->
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.removeJavascriptInterface("NativeBridgeAndroid")
                wv.stopLoading()
                wv.destroy()
            }
            webView = null
            isWidgetReady = false
            pendingCommands.clear()
            onOpenCallback = null
            onCloseCallback = null
        }
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
            logw("executeJs: WebView is null, dropping command")
            return
        }
        mainHandler.post {
            wv.evaluateJavascript(js, null)
        }
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
     * JavaScript interface that receives messages from the WebView.
     * Methods annotated with @JavascriptInterface are called from a background thread.
     */
    private inner class NativeBridgeInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            mainHandler.post {
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
    }

    private companion object {
        const val TAG = "SparrowDeskSDK"
    }
}
