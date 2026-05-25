package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event

sealed class GameEvent {
    class Start : Event()
    class Stop : Event()

    sealed class ModInit {
        class Pre : Event()
        class Post : Event()
    }
}