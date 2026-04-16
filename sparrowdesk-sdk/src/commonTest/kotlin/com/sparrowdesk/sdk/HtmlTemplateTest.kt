package com.sparrowdesk.sdk

import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlTemplateTest {

    private val config = SparrowDeskConfig(
        domain = "test.sparrowdesk.com",
        token = "abc123"
    )

    @Test
    fun htmlContainsWidgetToken() {
        val html = HtmlTemplate.build(config)
        assertTrue(html.contains("abc123"), "HTML should contain the widget token")
    }

    @Test
    fun htmlContainsDomain() {
        val html = HtmlTemplate.build(config)
        assertTrue(html.contains("test.sparrowdesk.com"), "HTML should contain the domain")
    }

    @Test
    fun htmlLoadsScriptFromCdn() {
        val html = HtmlTemplate.build(config)
        assertTrue(
            html.contains("https://assets.cdn.sparrowdesk.com/chatbot/bundle/main.js"),
            "HTML should load the widget script from CDN"
        )
    }

    @Test
    fun htmlSetsUpNativeBridgeCallbacks() {
        val html = HtmlTemplate.build(config)
        assertTrue(html.contains("NativeBridge.postMessage"), "HTML should set up NativeBridge")
    }

    @Test
    fun htmlSignalsWidgetReady() {
        val html = HtmlTemplate.build(config)
        assertTrue(
            html.contains("widgetReady"),
            "HTML should signal widgetReady to native"
        )
    }

    @Test
    fun htmlHasViewportMeta() {
        val html = HtmlTemplate.build(config)
        assertTrue(
            html.contains("viewport"),
            "HTML should have a viewport meta tag for mobile"
        )
    }

    @Test
    fun htmlEscapesTokenWithSpecialChars() {
        val dangerousConfig = SparrowDeskConfig(
            domain = "test.sparrowdesk.com",
            token = "token\"with<script>"
        )
        val html = HtmlTemplate.build(dangerousConfig)
        assertTrue(!html.contains("<script>with"), "Token with script tags should be escaped")
    }
}
