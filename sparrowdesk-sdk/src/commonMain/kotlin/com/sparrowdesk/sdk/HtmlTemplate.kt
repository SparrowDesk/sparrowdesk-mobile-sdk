package com.sparrowdesk.sdk

/**
 * Builds the HTML page loaded into the WebView.
 * Sets up the SparrowDesk widget script and the native bridge communication layer.
 */
internal object HtmlTemplate {

    fun build(config: SparrowDeskConfig): String {
        val escapedToken = JsBridge.escapeJs(config.token)
        val escapedDomain = JsBridge.escapeJs(config.domain)

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { width: 100%; height: 100%; overflow: hidden; background: transparent; }
    </style>
</head>
<body>
<script>
(function() {
    // Inject the SparrowDesk widget
    window.SD_WIDGET_TOKEN = "$escapedToken";
    window.SD_WIDGET_DOMAIN = "$escapedDomain";

    var script = document.createElement("script");
    script.src = "https://assets.cdn.sparrowdesk.com/chatbot/bundle/main.js";
    script.async = true;
    document.body.appendChild(script);

    // Poll for SparrowDesk API readiness
    var readyCheck = setInterval(function() {
        if (window.SparrowDesk) {
            clearInterval(readyCheck);

            // Wire up event listeners to native bridge
            window.SparrowDesk.onOpen(function() {
                if (window.NativeBridge) {
                    window.NativeBridge.postMessage("onOpen");
                }
            });

            window.SparrowDesk.onClose(function() {
                if (window.NativeBridge) {
                    window.NativeBridge.postMessage("onClose");
                }
            });

            // Signal native side that widget is ready
            if (window.NativeBridge) {
                window.NativeBridge.postMessage("widgetReady");
            }
        }
    }, 100);
})();
</script>
</body>
</html>
        """.trimIndent()
    }
}
