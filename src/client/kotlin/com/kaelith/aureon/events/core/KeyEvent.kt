package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event

sealed class KeyEvent {
    class Press(
        val keyCode: Int,
        val scanCode: Int,
        val modifiers: Int
    ) : Event(cancelable = true)

    class Release(
        val keyCode: Int,
        val scanCode: Int,
        val modifiers: Int
    ) : Event(cancelable = true)
}