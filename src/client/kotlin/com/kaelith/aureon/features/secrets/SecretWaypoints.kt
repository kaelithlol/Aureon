package com.kaelith.aureon.features.secrets

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.utils.Checkmark
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.DungeonEvent.Secrets.Type.*
import com.kaelith.aureon.features.secrets.utils.waypoints.*
import com.kaelith.aureon.utils.Utils
import net.minecraft.core.BlockPos
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object SecretWaypoints: Feature("secretWaypoints", island = SkyBlockIsland.THE_CATACOMBS) {
    data class CoordType(val rel: BlockPos, val real: BlockPos, val orig: SecretData.Coord )

    override fun initialize() {
        on<DungeonEvent.Secrets.Bat> { event -> updateSecret({ it.bat }) { Utils.calcDistance(event.blockPos, it.real) < 100 } }
        on<DungeonEvent.Secrets.Essence> { event -> updateSecret( { it.wither }) { it.real == event.blockPos } }
        on<DungeonEvent.Secrets.Chest> { event -> updateSecret( { it.chest }) { it.real == event.blockPos } }

        on<DungeonEvent.Secrets.Item> { event ->
            val pos = world?.getEntity(event.entityId)?.blockPosition() ?: return@on
            updateSecret({ it.item }) { Utils.calcDistance(pos, it.real) < 25 }
        }

        on<DungeonEvent.Secrets.Misc> { event ->
            when (event.secretType) {
                RED_SKULL -> updateSecret( { it.redstoneKey }) { it.real == event.blockPos }
                LEVER -> updateSecret( { it.lever }) { it.real == event.blockPos }
            }
        }

        on<ChatEvent.Receive> { event ->
            if (event.message.stripped.lowercase() != "that chest is locked!") return@on
            val pos = player?.blockPosition() ?: return@on
            updateSecret( {it.chest}, false) { Utils.calcDistance(pos, it.real) < 25 }
        }

        on<RenderEvent.World.Last> {
            if (Dungeon.inBoss) return@on
            val room = Dungeon.currentRoom ?: return@on
            val data = SecretsRegistry.getById(room.id) ?: return@on
            if(room.checkmark == Checkmark.GREEN) return@on

            data.toWaypoints(config, room).forEach { it.render() }
        }

        SecretsRegistry.load()
    }

    private fun updateSecret(findList: (SecretData) -> List<SecretData.Coord>, collect: Boolean = true, predicate: (CoordType) -> Boolean) {
        val room = Dungeon.currentRoom ?: return
        val data = SecretsRegistry.getById(room.id) ?: return

        findList(data).find { coord ->
            val coordPos = coord.toBlockPos()
            val realPos = room.getRealCoord(coordPos);
            predicate(CoordType(coordPos, realPos, coord))
        }?.collected = collect
    }
}