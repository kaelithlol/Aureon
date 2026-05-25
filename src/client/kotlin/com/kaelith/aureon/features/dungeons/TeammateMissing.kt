package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.Utils
import com.kaelith.aureon.api.dungeons.Dungeon
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object TeammateMissing: Feature("teammateMissing", island = SkyBlockIsland.THE_CATACOMBS) {
    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            if(event.message.stripped != "Starting in 4 seconds.") return@on
            val players = Dungeon.players.size
            if (players < 5) Utils.alert("§c$players/5 Players!", SoundEvents.NOTE_BLOCK_PLING.value())
        }
    }
}