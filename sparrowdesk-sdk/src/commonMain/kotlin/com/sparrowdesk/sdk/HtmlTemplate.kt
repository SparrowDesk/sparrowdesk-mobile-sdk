package com.sparrowdesk.sdk

/**
 * Builds the HTML page loaded into the WebView.
 * Sets up the SparrowDesk widget script and the native bridge communication layer.
 */
internal object HtmlTemplate {

    fun build(config: SparrowDeskConfig): String {
        val escapedToken = JsBridge.escapeJs(config.token)
        val escapedDomain = JsBridge.escapeJs(config.domain)
        val debug = if (config.debug) "true" else "false"

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
    var DEBUG = $debug;
    function log(msg) { if (DEBUG) console.log("[SD] " + msg); }
    function warn(msg) { if (DEBUG) console.warn("[SD] " + msg); }

    // Inject the SparrowDesk widget
    window.SD_WIDGET_TOKEN = "$escapedToken";
    window.SD_WIDGET_DOMAIN = "$escapedDomain";

    var script = document.createElement("script");
    script.src = "https://assets.cdn.sparrowdesk.com/chatbot/bundle/main.js";
    script.async = true;
    script.onload = function() { log("bundle main.js onload"); };
    script.onerror = function(e) { warn("bundle main.js failed to load"); };
    document.body.appendChild(script);

    function describe(obj) {
        if (!obj) return String(obj);
        var shape = {};
        for (var k in obj) {
            try { shape[k] = typeof obj[k]; } catch (e) { shape[k] = "?"; }
        }
        return JSON.stringify(shape);
    }

    var tries = 0;
    var readyCheck = setInterval(function() {
        tries++;
        var api = window.sparrowDesk || window.SparrowDesk;
        // Wait for BOTH the API object AND the bundle's launcher-ready flag;
        // the object can exist before internal init finishes, which caused
        // the first-load openWidget() to be dropped.
        if (api && window.SD_LAUNCHER_READY) {
            clearInterval(readyCheck);
            log("sparrowDesk ready after " + tries + " tries; shape=" + describe(api));

            if (typeof api.onOpen === "function") {
                api.onOpen(function() {
                    if (window.NativeBridge) window.NativeBridge.postMessage("onOpen");
                });
            } else {
                warn("sparrowDesk.onOpen is not a function");
            }
            if (typeof api.onClose === "function") {
                api.onClose(function() {
                    if (window.NativeBridge) window.NativeBridge.postMessage("onClose");
                });
            } else {
                warn("sparrowDesk.onClose is not a function");
            }

            if (window.NativeBridge) window.NativeBridge.postMessage("widgetReady");
            return;
        }
        // Diagnostic timeouts.
        if (DEBUG && (tries === 30 || tries === 100)) {
            var candidates = [];
            for (var k in window) {
                if (/sparrow|chatbot|widget|sd|survey|launcher/i.test(k)) candidates.push(k);
            }
            warn("widgetReady timeout after " + (tries * 100) + "ms. " +
                 "api=" + !!api + " launcherReady=" + !!window.SD_LAUNCHER_READY +
                 " globals=" + JSON.stringify(candidates));
        }
    }, 100);
})();
</script>
</body>
</html>
        """.trimIndent()
    }
}
