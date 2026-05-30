package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import java.awt.Color
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Module
object ClockDisplay : Feature("clockDisplay") {
    private const val NAME = "clockDisplay"
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val color by config.property<Color>("clockDisplay.color")

    override fun initialize() {
        HUDManager.registerCustom(NAME, 44, 12, this::editorRender, "clockDisplay")
        on<GuiEvent.RenderHUD> { render(it.context) }
    }

    private fun editorRender(context: GuiGraphicsExtractor) {
        Render2D.drawString(context, "12:34", 2, 2, color = color)
    }

    private fun render(context: GuiGraphicsExtractor) {
        HUDManager.renderHud(NAME, context) {
            Render2D.drawString(context, LocalTime.now().format(formatter), 0, 0, color = color)
        }
    }
}
