package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.utils.config
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object LeapAnnounce: Feature("leapAnnounce", island = SkyBlockIsland.THE_CATACOMBS) {
    private val leapRegex = "^You have teleported to (\\w+)".toRegex()
    private val message by config.property<String>("leapAnnounce.message")

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val person = leapRegex.find(event.message.stripped)?.groups?.get(1)?.value ?: return@on
            val finalMessage = if (message.contains($$"$player")) message.replace($$"$player", person) else "$message $person"
            Signal.sendCommand("pc $finalMessage")
        }
    }
}