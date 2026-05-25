package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.events.core.GuiEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import net.minecraft.client.gui.GuiGraphicsExtractor


@Module
object RoomName : Feature("showRoomName", island = SkyBlockIsland.THE_CATACOMBS) {
    private val chroma by config.property<Boolean>("roomNameChroma")

    override fun initialize() {
        HUDManager.register("roomname", "No Room Found", "showRoomName")
        on<GuiEvent.RenderHUD> { renderHUD(it.context) }
    }

    private fun renderHUD(context: GuiGraphicsExtractor) = HUDManager.renderHud("roomname", context) {
        if (Dungeon.inBoss) return@renderHud
        val text = "${if (chroma) "§z" else ""}${Dungeon.currentRoom?.name ?: "No Room Found"}"
        Render2D.drawString(context,text, 0, 0, shadow = false)
    }
}