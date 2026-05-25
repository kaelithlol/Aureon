package com.kaelith.aureon.api.events

open class Event(val cancelable: Boolean = false) {
    var cancelled: Boolean = false
        private set
    fun cancel() { if (cancelable) cancelled = true }
}