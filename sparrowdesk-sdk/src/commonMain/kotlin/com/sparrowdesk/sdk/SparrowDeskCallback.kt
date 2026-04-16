package com.sparrowdesk.sdk

/**
 * Callback interface for widget events.
 * SAM-convertible — can be used as a lambda on both platforms.
 */
fun interface SparrowDeskCallback {
    fun onEvent()
}
