package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.ServerEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import java.awt.Color
import java.util.ArrayDeque
import kotlin.math.roundToInt

@Module
object TpsDisplay : Feature("tpsDisplay") {
    private const val NAME = "tpsDisplay"
    private val color by config.property<Color>("tpsDisplay.color")
    private val intervals = ArrayDeque<Long>()
    private var lastTick = 0L
    private var tps = 20.0

    override fun initialize() {
        HUDManager.registerCustom(NAME, 48, 12, this::editorRender, "tpsDisplay")
        on<TickEvent.Server> { updateTps() }
        on<ServerEvent.Disconnect> { reset() }
        on<GuiEvent.RenderHUD> { render(it.context) }
    }

    private fun editorRender(context: GuiGraphicsExtractor) {
        Render2D.drawString(context, "20.0 tps", 2, 2, color = color)
    }

    private fun updateTps() {
        val now = System.currentTimeMillis()
        if (lastTick != 0L) {
            intervals.addLast(now - lastTick)
            while (intervals.size > 40) intervals.removeFirst()
            val average = intervals.average().coerceAtLeast(1.0)
            tps = (1000.0 / average).coerceIn(0.0, 20.0)
        }
        lastTick = now
    }

    private fun reset() {
        intervals.clear()
        lastTick = 0L
        tps = 20.0
    }

    private fun render(context: GuiGraphicsExtractor) {
        val rounded = (tps * 10.0).roundToInt() / 10.0
        HUDManager.renderHud(NAME, context) {
            Render2D.drawString(context, "$rounded tps", 0, 0, color = color)
        }
    }
}
