package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.utils.Checkmark
import com.kaelith.aureon.api.dungeons.utils.RoomType
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object RoomAlerts : Feature("roomAlerts", island = SkyBlockIsland.THE_CATACOMBS) {
    private val clear by config.property<Boolean>("roomAlerts.clear")
    private val secrets by config.property<Boolean>("roomAlerts.secrets")

    override fun initialize() {
        on<DungeonEvent.Room.StateChange> { event ->
            val room = event.room
            if (room != Dungeon.currentRoom) return@on
            if (room.type !in setOf(RoomType.NORMAL, RoomType.PUZZLE, RoomType.RARE, RoomType.TRAP, RoomType.YELLOW)) return@on
            if (room.type == RoomType.PUZZLE && room.name != "Blaze") return@on

            when (event.newState) {
                Checkmark.WHITE -> if (clear) alert(if (room.secrets == 0) "§aCleared" else "§fCleared")
                Checkmark.GREEN -> if (secrets && room.secrets > 0) alert("§aSecrets Done!")
                else -> {}
            }
        }
    }

    private fun alert(text: String) {
        client.gui.setTitle(Component.literal(text))
        client.gui.setTimes(5, 25, 5)
        client.player?.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1.25f)
    }
}
