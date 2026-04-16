package com.sparrowdesk.sdk

import kotlin.test.Test
import kotlin.test.assertEquals

class WidgetStatusTest {

    @Test
    fun fromStringOpen() {
        assertEquals(WidgetStatus.OPEN, WidgetStatus.fromString("open"))
    }

    @Test
    fun fromStringClosed() {
        assertEquals(WidgetStatus.CLOSED, WidgetStatus.fromString("closed"))
    }

    @Test
    fun fromStringCaseInsensitive() {
        assertEquals(WidgetStatus.OPEN, WidgetStatus.fromString("OPEN"))
        assertEquals(WidgetStatus.CLOSED, WidgetStatus.fromString("Closed"))
    }

    @Test
    fun fromStringUnknownValue() {
        assertEquals(WidgetStatus.UNKNOWN, WidgetStatus.fromString("something"))
        assertEquals(WidgetStatus.UNKNOWN, WidgetStatus.fromString(""))
    }
}
