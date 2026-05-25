package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.handlers.Chronos
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object DungeonQoL : Feature("dungeonQoL", island = SkyBlockIsland.THE_CATACOMBS) {
    private val autoRequeue by config.property<Boolean>("dungeonQoL.autoRequeue")
    private val extraStats by config.property<Boolean>("dungeonQoL.extraStats")
    private val mimicAnnounce by config.property<Boolean>("dungeonQoL.mimicAnnounce")
    private val princeAnnounce by config.property<Boolean>("dungeonQoL.princeAnnounce")
    private val secretSounds by config.property<Boolean>("dungeonQoL.secretSounds")

    override fun initialize() {
        on<DungeonEvent.End> {
            if (extraStats) Chronos.Tick after 20 run { Signal.sendCommand("showextrastats") }
            if (autoRequeue) Chronos.Tick after 40 run { Signal.sendCommand("instancerequeue") }
        }

        on<DungeonEvent.Score.MimicDead> {
            if (mimicAnnounce) Signal.sendCommand("pc Mimic dead!")
        }

        on<DungeonEvent.Score.PrinceDead> {
            if (princeAnnounce) Signal.sendCommand("pc Prince dead!")
        }

        on<DungeonEvent.Secrets.Item> { playSecretSound() }
        on<DungeonEvent.Secrets.Bat> { playSecretSound() }
        on<DungeonEvent.Secrets.Chest> { playSecretSound() }
        on<DungeonEvent.Secrets.Essence> { playSecretSound() }
        on<DungeonEvent.Secrets.Misc> { playSecretSound() }
    }

    private fun playSecretSound() {
        if (!secretSounds) return
        player?.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1.35f)
    }
}
