package com.sparrowdesk.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
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
        if (webView != null) return // Already attached

        val wv = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false

            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

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
        mainHandler.post {
            wv.evaluateJavascript(JsBridge.getStatus()) { result ->
                val status = WidgetStatus.fromString(
                    result?.removeSurrounding("\"") ?: ""
                )
                callback(status)
            }
        }
    }

    actual fun show() {
        mainHandler.post { webView?.visibility = View.VISIBLE }
    }

    actual fun hide() {
        mainHandler.post { webView?.visibility = View.GONE }
    }

    actual fun destroy() {
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
            executeJs(js)
        } else {
            pendingCommands.add(js)
        }
    }

    private fun executeJs(js: String) {
        val wv = webView ?: return
        mainHandler.post {
            wv.evaluateJavascript(js, null)
        }
    }

    private fun flushPendingCommands() {
        pendingCommands.forEach { executeJs(it) }
        pendingCommands.clear()
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
                        isWidgetReady = true
                        flushPendingCommands()
                    }
                    "onOpen" -> onOpenCallback?.onEvent()
                    "onClose" -> onCloseCallback?.onEvent()
                }
            }
        }
    }
}
