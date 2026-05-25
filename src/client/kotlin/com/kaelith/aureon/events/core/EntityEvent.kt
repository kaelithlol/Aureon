package com.kaelith.aureon.events.core


import net.minecraft.world.entity.Entity
import com.kaelith.aureon.api.events.Event

sealed class EntityEvent {
    class Death(
        val entity: Entity
    ) : Event()
}