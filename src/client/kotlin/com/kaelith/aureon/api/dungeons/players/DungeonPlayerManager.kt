package com.kaelith.aureon.api.dungeons.players

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.api.dungeons.utils.Checkmark
import com.kaelith.aureon.events.EventBus
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.TablistEvent
import com.kaelith.aureon.api.dungeons.utils.DungeonClass
import com.kaelith.aureon.api.zenith.player
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

object DungeonPlayerManager {
    private const val TAB_PLAYER_OFFSET = 1
    private const val TAB_PLAYER_STRIDE = 4

    // Modified from Skyblocker — filters rank prefixes, ironman icons, and emblems
    // Groups: name, class (or "EMPTY" before map start), level (Roman numerals, or absent)
    val playerTabPattern = Regex("\\[\\d+] (?:\\[[A-Za-z]+] )?(?<name>[A-Za-z0-9_]+) (?:.+ )?\\((?<class>\\S+) ?(?<level>[LXVI0]+)?\\)")
    val playerGhostPattern = Regex(" ☠ (?<name>[A-Za-z0-9_]+) .+ became a ghost\\.")

    val players = Array<DungeonPlayer?>(5) { null }

    fun init() {
        EventBus.on<TablistEvent.Change>(SkyBlockIsland.THE_CATACOMBS){ event ->
            val firstColumn = event.new.firstOrNull() ?: return@on

            for (i in 0 until 5) {
                val index = TAB_PLAYER_OFFSET + i * TAB_PLAYER_STRIDE
                if (index !in firstColumn.indices) continue
                val match = playerTabPattern.find(firstColumn[index].stripped)
                if (match == null) {
                    players[i] = null
                    continue
                }

                val name = match.groups["name"]?.value ?: continue
                val clazz = DungeonClass.from(match.groups["class"]?.value ?: "EMPTY")

                val existing = getPlayer(name)
                if (existing != null) {
                    players[i] = existing
                    existing.dclass = clazz
                } else {
                    players[i] = DungeonPlayer(name).apply { dclass = clazz }
                }
            }
        }

        EventBus.on<ChatEvent.Receive>(SkyBlockIsland.THE_CATACOMBS) { onDeath(it.message.string) }
    }


    private fun onDeath(text: String) {
        val match = playerGhostPattern.find(text) ?: return

        var name = match.groups["name"]?.value ?: return
        if (name == "You") player?.let { name = it.name.stripped }

        val player = getPlayer(name)
        if (player != null) {
            player.deaths ++
            EventBus.post(DungeonEvent.Player.Death(player))
        } else {
            AureonCore.LOGGER.error(
                "[Dungeon Player Manager] Received ghost message for player '{}' but player was not found in the player list: {}",
                match.groups["name"]?.value,
                players.contentToString()
            )
        }
    }

    fun getPlayer(name: String): DungeonPlayer? {
        return players
            .asSequence()
            .filterNotNull()
            .firstOrNull { it.name == name }
    }

    fun reset() {
        players.fill(null)
    }
}