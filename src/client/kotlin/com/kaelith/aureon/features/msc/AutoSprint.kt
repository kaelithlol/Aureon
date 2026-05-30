package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature

@Module
object AutoSprint : Feature("autoSprint") {
    override fun initialize() {
        on<TickEvent.Client> {
            val player = client.player ?: return@on
            if (player.isSprinting) return@on
            client.options.keySprint.isDown = true
        }
    }
}
