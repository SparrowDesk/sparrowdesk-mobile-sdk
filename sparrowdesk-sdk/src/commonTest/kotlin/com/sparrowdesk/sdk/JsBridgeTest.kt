package com.sparrowdesk.sdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsBridgeTest {

    @Test
    fun openWidgetProducesCorrectJs() {
        assertEquals("window.SparrowDesk.openWidget();", JsBridge.openWidget())
    }

    @Test
    fun closeWidgetProducesCorrectJs() {
        assertEquals("window.SparrowDesk.closeWidget();", JsBridge.closeWidget())
    }

    @Test
    fun hideWidgetProducesCorrectJs() {
        assertEquals("window.SparrowDesk.hideWidget();", JsBridge.hideWidget())
    }

    @Test
    fun getStatusProducesCorrectJs() {
        assertEquals("window.SparrowDesk.status", JsBridge.getStatus())
    }

    @Test
    fun setTagsWithSingleTag() {
        val result = JsBridge.setTags(listOf("support"))
        assertEquals("window.SparrowDesk.setTags([\"support\"]);", result)
    }

    @Test
    fun setTagsWithMultipleTags() {
        val result = JsBridge.setTags(listOf("vip", "premium"))
        assertEquals("window.SparrowDesk.setTags([\"vip\",\"premium\"]);", result)
    }

    @Test
    fun setTagsWithEmptyList() {
        val result = JsBridge.setTags(emptyList())
        assertEquals("window.SparrowDesk.setTags([]);", result)
    }

    @Test
    fun setContactFieldsProducesCorrectJs() {
        val result = JsBridge.setContactFields(mapOf("email" to "test@example.com"))
        assertEquals(
            "window.SparrowDesk.setContactFields({\"email\":\"test@example.com\"});",
            result
        )
    }

    @Test
    fun setConversationFieldsProducesCorrectJs() {
        val result = JsBridge.setConversationFields(mapOf("priority" to "high"))
        assertEquals(
            "window.SparrowDesk.setConversationFields({\"priority\":\"high\"});",
            result
        )
    }

    @Test
    fun escapeJsHandlesQuotes() {
        val escaped = JsBridge.escapeJs("""He said "hello" and 'bye'""")
        assertEquals("""He said \"hello\" and \'bye\'""", escaped)
    }

    @Test
    fun escapeJsHandlesBackslashes() {
        val escaped = JsBridge.escapeJs("path\\to\\file")
        assertEquals("path\\\\to\\\\file", escaped)
    }

    @Test
    fun escapeJsHandlesNewlines() {
        val escaped = JsBridge.escapeJs("line1\nline2\rline3")
        assertEquals("line1\\nline2\\rline3", escaped)
    }

    @Test
    fun escapeJsPreventsScriptInjection() {
        val escaped = JsBridge.escapeJs("</script><script>alert(1)</script>")
        assertTrue(!escaped.contains("<"))
        assertTrue(!escaped.contains(">"))
    }

    @Test
    fun setContactFieldsEscapesValues() {
        val result = JsBridge.setContactFields(mapOf("name" to "O'Brien"))
        assertTrue(result.contains("\\'"))
    }
}
