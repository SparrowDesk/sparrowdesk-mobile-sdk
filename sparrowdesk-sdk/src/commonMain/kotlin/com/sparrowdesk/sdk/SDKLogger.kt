package com.sparrowdesk.sdk

internal expect object SDKLogger {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String)
}
