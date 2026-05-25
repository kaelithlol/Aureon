package com.kaelith.aureon.features.aureonnav

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.render.*
import com.kaelith.aureon.utils.render.Render2D.width
import com.kaelith.aureon.api.dungeons.Dungeon
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import net.minecraft.client.gui.GuiGraphicsExtractor

@Module
object MapInfo: Feature("separateMapInfo", island = SkyBlockIsland.THE_CATACOMBS) {
    const val name = "Map Info"

    override fun initialize() {
        HUDManager.registerCustom(name, 200, 30,this::hudEditorRender, "separateMapInfo")

        on<GuiEvent.RenderHUD> { event -> renderNormal(event.context) }
    }

    fun hudEditorRender(context: GuiGraphicsExtractor){
        renderMapInfo(context, true)
    }

    fun renderNormal(context: GuiGraphicsExtractor) {
        val matrix = context.pose()

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        matrix.pushMatrix()
        matrix.translate(x, y)
        matrix.scale(scale, scale)

        renderMapInfo(context, false)

        matrix.popMatrix()
    }

    fun renderMapInfo(context: GuiGraphicsExtractor, preview: Boolean) {
        val matrix = context.pose()

        var mapLine1 = Dungeon.mapLine1
        var mapLine2 = Dungeon.mapLine2

        if (preview) {
            mapLine1 = "§7Secrets: §b?§8-§e?§8-§c?        §7Score: §c0"
            mapLine2 = "§7Deaths: §a0 §8| §7M: §c✘ §8| §7P: §c✘ §8| §7Crypts: §c0"
        }
        val w1 = mapLine1.width()
        val w2 = mapLine2.width()

        matrix.pushMatrix()
        matrix.translate( 100f, 5f,)

        Render2D.drawString(context, mapLine1,-w1 / 2, 0)
        Render2D.drawString(context, mapLine2,-w2 / 2, 10)

        matrix.popMatrix()
    }
}
