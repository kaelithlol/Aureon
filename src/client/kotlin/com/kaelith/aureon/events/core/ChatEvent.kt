package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event
import net.minecraft.network.chat.Component

sealed class ChatEvent {
    class Receive(val message: Component, val isActionBar: Boolean) : Event(cancelable = true)
}