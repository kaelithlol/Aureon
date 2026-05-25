package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event

sealed class TickEvent {
    class Client: Event()
    class Server: Event()
}