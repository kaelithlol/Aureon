package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object AutoGFS : Feature("autoGfs", island = SkyBlockIsland.THE_CATACOMBS) {
    private val delaySeconds by config.property<Int>("autoGfs.delay")
    private val pearls by config.property<Boolean>("autoGfs.pearls")
    private val superboom by config.property<Boolean>("autoGfs.superboom")
    private val jerry by config.property<Boolean>("autoGfs.jerry")
    private val leaps by config.property<Boolean>("autoGfs.leaps")
    private val twilight by config.property<Boolean>("autoGfs.twilight")
    private var lastCheck = 0L
    private var stormLightningFetched = false

    private val p5Message = Regex("^\\[BOSS] Wither King: I no longer wish to fight, but I know that will not stop you\\.$")
    private val stormLightning = Regex("^\\[BOSS] Storm: (ENERGY HEED MY CALL|THUNDER LET ME BE YOUR CATALYST)!$")

    override fun initialize() {
        on<LocationEvent.ServerChange> { stormLightningFetched = false }

        on<TickEvent.Client> {
            if (client.screen != null || player == null) return@on
            val now = System.currentTimeMillis()
            if (now - lastCheck < delaySeconds * 1000L) return@on
            lastCheck = now
            refill()
        }

        on<ChatEvent.Receive> { event ->
            if (!twilight || Dungeon.floor != DungeonFloor.M7 || !Dungeon.inBoss) return@on
            val msg = event.message.stripped
            when {
                p5Message.matches(msg) -> Signal.sendCommand("gfs twilight_arrow_poison 8")
                !stormLightningFetched && stormLightning.matches(msg) -> {
                    Signal.sendCommand("gfs twilight_arrow_poison 8")
                    stormLightningFetched = true
                }
            }
        }
    }

    private fun refill() {
        val inv = player?.inventory ?: return
        var pearlCount = 0
        var jerryCount = 0
        var tntCount = 0
        var leapCount = 0

        for (i in 0 until inv.containerSize) {
            val stack = inv.getItem(i)
            when (stack.getSkyBlockId()) {
                "ENDER_PEARL" -> pearlCount += stack.count
                "INFLATABLE_JERRY" -> jerryCount += stack.count
                "SUPERBOOM_TNT" -> tntCount += stack.count
                "SPIRIT_LEAP" -> leapCount += stack.count
            }
        }

        checkAndRefill(pearlCount, 16, "ender_pearl", pearls)
        checkAndRefill(jerryCount, 64, "inflatable_jerry", jerry)
        checkAndRefill(tntCount, 64, "superboom_tnt", superboom)
        checkAndRefill(leapCount, 16, "spirit_leap", leaps)
    }

    private fun checkAndRefill(current: Int, max: Int, item: String, enabled: Boolean) {
        if (!enabled || current <= 0) return
        val needed = max - current
        if (needed >= 4) Signal.sendCommand("gfs $item $needed")
    }
}
