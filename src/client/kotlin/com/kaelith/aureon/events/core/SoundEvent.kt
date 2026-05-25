package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

sealed class SoundEvent {
    class Play(
        val pos: Vec3,
        val sound: SoundEvent,
        val source: SoundSource,
        val volume: Float,
        val pitch: Float
    ) : Event(cancelable = true)
}