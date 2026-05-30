package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.mixins.accessors.AccessorLerpingBossEvent
import com.kaelith.aureon.utils.config
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.util.Locale
import kotlin.math.roundToInt

@Module
object BossBarHealth : Feature("bossBarHealth", island = SkyBlockIsland.THE_CATACOMBS) {
    private val watcher by config.property<Boolean>("bossBarHealth.watcher")
    private val thorn by config.property<Boolean>("bossBarHealth.thorn")
    private val withers by config.property<Boolean>("bossBarHealth.withers")

    @JvmStatic
    fun appendHealth(event: LerpingBossEvent, originalName: Component): Component {
        if (!isEnabled() || LocationAPI.island != SkyBlockIsland.THE_CATACOMBS) return originalName
        val maxHealth = getMaxHealth(originalName) ?: return originalName
        val current = (((event as AccessorLerpingBossEvent).aureonTargetPercent * maxHealth).roundToInt()).toFloat()
        return originalName.copy().append(Component.literal(" §r§8- §a${formatHealth(current)}§7/§a${formatHealth(maxHealth)}§c❤"))
    }

    private fun getMaxHealth(nameComponent: Component): Float? {
        val name = nameComponent.stripped
        val isMaster = DungeonAPI.dungeonFloor?.name?.startsWith("M") == true

        return when (name) {
            "The Watcher" -> if (watcher) 12f + (DungeonAPI.dungeonFloor?.floorNumber?.toFloat() ?: 0f) else null
            "Thorn" -> if (thorn) if (isMaster) 6f else 4f else null
            "Maxor" -> if (withers) if (isMaster) 800_000_000f else 100_000_000f else null
            "Storm" -> if (withers) if (isMaster) 1_000_000_000f else 400_000_000f else null
            "Goldor" -> if (withers) if (isMaster) 1_200_000_000f else 750_000_000f else null
            "Necron" -> if (withers) if (isMaster) 1_400_000_000f else 1_000_000_000f else null
            else -> null
        }
    }

    private fun formatHealth(health: Float): String = when {
        health >= 1_000_000_000f -> {
            val value = health / 1_000_000_000f
            if (value % 1.0f == 0f) "${value.toInt()}B" else String.format(Locale.US, "%.1fB", value)
        }
        health >= 1_000_000f -> "${(health / 1_000_000f).toInt()}M"
        health >= 1_000f -> "${(health / 1_000f).toInt()}k"
        else -> health.toInt().toString()
    }
}
