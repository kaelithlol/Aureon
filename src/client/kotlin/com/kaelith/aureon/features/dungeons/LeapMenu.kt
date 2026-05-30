package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.config.core.Keybind
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object LeapMenu : Feature("leapMenu", island = SkyBlockIsland.THE_CATACOMBS) {
    private val announce by config.property<Boolean>("leapMenu.announce")
    private val message by config.property<String>("leapMenu.message")
    private val hideNearby by config.property<Boolean>("leapMenu.hideNearby")
    private val slot1 by config.property<Keybind.Handler>("leapMenu.slot1")
    private val slot2 by config.property<Keybind.Handler>("leapMenu.slot2")
    private val slot3 by config.property<Keybind.Handler>("leapMenu.slot3")
    private val slot4 by config.property<Keybind.Handler>("leapMenu.slot4")
    private val binds get() = listOf(slot1, slot2, slot3, slot4)

    private val leapRegex = Regex("^You have teleported to (\\w+)!?$")
    private val playerRegex = Regex("(?:\\[.+?] )?(\\w+)")
    private var hideUntil = 0L

    override fun initialize() {
        binds.forEachIndexed { index, bind -> bind.onPress { clickLeapSlot(index) } }

        on<ChatEvent.Receive> { event ->
            val name = leapRegex.find(event.message.stripped)?.groupValues?.get(1) ?: return@on
            if (announce) {
                val msg = message.replace("{name}", name).takeIf { it.isNotBlank() } ?: "Leaping to $name"
                Signal.sendCommand("pc $msg")
            }
            if (hideNearby) hideUntil = System.currentTimeMillis() + 3500L
        }

        on<GuiEvent.Key> { event ->
            if (!isLeapScreen()) return@on
            binds.forEachIndexed { index, bind ->
                if (bind.keyCode() == event.key) {
                    event.cancel()
                    clickLeapSlot(index)
                    return@on
                }
            }
        }

        on<RenderEvent.Entity.Pre> { event ->
            if (!hideNearby || System.currentTimeMillis() > hideUntil) return@on
            val entity = event.entity as? Player ?: return@on
            val self = player ?: return@on
            if (entity == self || entity.distanceToSqr(self) > 16.0) return@on
            if (Dungeon.players.none { it.name == entity.gameProfile.name }) return@on
            event.cancel()
        }
    }

    private fun isLeapScreen(): Boolean {
        val title = client.screen?.title?.stripped?.lowercase() ?: return false
        return Dungeon.inDungeon && (title.contains("spirit leap") || title.contains("teleport to player"))
    }

    private fun clickLeapSlot(index: Int) {
        if (!isLeapScreen()) return
        val screen = client.screen as? AbstractContainerScreen<*> ?: return
        val localPlayer = player ?: return
        val slot = screen.menu.slots
            .filter { it.item.`is`(Items.PLAYER_HEAD) }
            .sortedBy { playerRegex.find(it.item.hoverName.stripped)?.groupValues?.getOrNull(1) ?: it.item.hoverName.stripped }
            .getOrNull(index)
            ?: return

        client.gameMode?.handleContainerInput(screen.menu.containerId, slot.index, 0, ContainerInput.PICKUP, localPlayer)
        localPlayer.closeContainer()
    }
}
