package com.kaelith.aureon.events.core

import net.minecraft.network.chat.Component
import com.kaelith.aureon.api.events.Event

sealed class TablistEvent {
    class Change(
        val old: List<List<String>>,
        val new: List<List<Component>>,
    ) : Event()
}