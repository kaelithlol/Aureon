package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.utils.RoomType
import com.kaelith.aureon.api.handlers.Chronos
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object ArchitectDraft : Feature("architectDraft", island = SkyBlockIsland.THE_CATACOMBS) {
    private val announce by config.property<Boolean>("architectDraft.announce")
    private val autoGet by config.property<Boolean>("architectDraft.autoGet")
    private val resetPattern = Regex("^You used the Architect's First Draft to reset (.+)!$")
    private val failPattern = Regex("^PUZZLE FAIL! (?<player>\\w{1,16}) .+$")
    private val quizFailPattern = Regex("^\\[STATUE] Oruo the Omniscient: (?<player>\\w{1,16}) chose the wrong answer!.*$")

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            if (Dungeon.inBoss || Dungeon.currentRoom?.type != RoomType.PUZZLE) return@on
            val msg = event.message.stripped

            if (announce) {
                resetPattern.find(msg)?.groupValues?.getOrNull(1)?.let { puzzle ->
                    Signal.sendCommand("pc Used Draft to Reset $puzzle")
                }
            }

            if (!autoGet) return@on
            val playerName = failPattern.find(msg)?.groups?.get("player")?.value
                ?: quizFailPattern.find(msg)?.groups?.get("player")?.value
                ?: return@on

            if (playerName == client.user.name) {
                Chronos.Tick after 30 run { Signal.sendCommand("gfs ARCHITECT_FIRST_DRAFT 1") }
            }
        }
    }
}
