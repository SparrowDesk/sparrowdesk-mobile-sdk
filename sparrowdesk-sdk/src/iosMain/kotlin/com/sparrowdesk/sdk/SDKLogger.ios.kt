package com.sparrowdesk.sdk

internal actual object SDKLogger {
    actual fun d(tag: String, message: String) {
        println("[$tag] D: $message")
    }

    actual fun w(tag: String, message: String) {
        println("[$tag] W: $message")
    }

    actual fun e(tag: String, message: String) {
        println("[$tag] E: $message")
    }
}
