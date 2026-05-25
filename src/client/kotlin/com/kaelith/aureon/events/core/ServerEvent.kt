package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event

sealed class ServerEvent {
    class Connect : Event()

    class Disconnect : Event()
}