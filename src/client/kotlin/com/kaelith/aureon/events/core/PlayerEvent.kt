package com.kaelith.aureon.events.core

import net.minecraft.world.item.ItemStack
import com.kaelith.aureon.api.events.Event

sealed class PlayerEvent {
    class HotbarChange(
        val slot: Int,
        val item: ItemStack
    ) : Event()
}