package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.Zenith
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import java.awt.Color
import java.util.ArrayDeque

@Module
object CpsDisplay : Feature("cpsDisplay") {
    private const val NAME = "cpsDisplay"
    private val color by config.property<Color>("cpsDisplay.color")
    private val leftClicks = ArrayDeque<Long>()
    private val rightClicks = ArrayDeque<Long>()
    private var leftDown = false
    private var rightDown = false

    override fun initialize() {
        HUDManager.registerCustom(NAME, 64, 12, this::editorRender, "cpsDisplay")
        on<TickEvent.Client> { updateClicks() }
        on<GuiEvent.RenderHUD> { render(it.context) }
    }

    private fun editorRender(context: GuiGraphicsExtractor) {
        Render2D.drawString(context, "0 | 0 cps", 2, 2, color = color)
    }

    private fun updateClicks() {
        val now = System.currentTimeMillis()
        val leftPressed = Zenith.Mouse.isPressed(Zenith.Mouse.LEFT)
        val rightPressed = Zenith.Mouse.isPressed(Zenith.Mouse.RIGHT)

        if (leftPressed && !leftDown) leftClicks.addLast(now)
        if (rightPressed && !rightDown) rightClicks.addLast(now)
        leftDown = leftPressed
        rightDown = rightPressed

        prune(leftClicks, now)
        prune(rightClicks, now)
    }

    private fun prune(clicks: ArrayDeque<Long>, now: Long) {
        while (clicks.isNotEmpty() && now - clicks.first() > 1000L) clicks.removeFirst()
    }

    private fun render(context: GuiGraphicsExtractor) {
        HUDManager.renderHud(NAME, context) {
            Render2D.drawString(context, "${leftClicks.size} | ${rightClicks.size} cps", 0, 0, color = color)
        }
    }
}
