package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.utils.Utils
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.score.DungeonScore
import com.kaelith.aureon.api.handlers.Chronos
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.LocationEvent
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.location.*
import kotlin.time.Duration.Companion.minutes

@Module
object CryptReminder: Feature("cryptReminder", island = SkyBlockIsland.THE_CATACOMBS) {
    private val delay by config.property<Int>("cryptReminder.delay")
    private val party by config.property<Boolean>("cryptReminder.sendParty")

    private var reminderHandle: Chronos.Task? = null
    private val crypts get() = DungeonScore.data.crypts

    override fun initialize() {
        on<DungeonEvent.Start> { event ->
            reminderHandle?.cancel()
            reminderHandle = Chronos.Async after delay.minutes given { crypts < 5 && LocationAPI.island == SkyBlockIsland.THE_CATACOMBS && !Dungeon.inBoss } run {
                Utils.alert("§dCrypts: §c$crypts§7/§c5", SoundEvents.NOTE_BLOCK_PLING.value())
                if (party) Signal.sendCommand("pc $crypts/5 crypts")
            }
        }

        on<LocationEvent.ServerChange> {
            reminderHandle?.cancel()
            reminderHandle = null
        }
    }
}