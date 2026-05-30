package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import java.awt.Color

@Module
object FpsDisplay : Feature("fpsDisplay") {
    private const val NAME = "fpsDisplay"
    private val color by config.property<Color>("fpsDisplay.color")

    override fun initialize() {
        HUDManager.registerCustom(NAME, 54, 12, this::editorRender, "fpsDisplay")
        on<GuiEvent.RenderHUD> { render(it.context) }
    }

    private fun editorRender(context: GuiGraphicsExtractor) {
        Render2D.drawString(context, "120 fps", 2, 2, color = color)
    }

    private fun render(context: GuiGraphicsExtractor) {
        HUDManager.renderHud(NAME, context) {
            Render2D.drawString(context, "${client.fps} fps", 0, 0, color = color)
        }
    }
}
