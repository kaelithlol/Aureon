package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.api.orbit.Orbit.toVec3
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.api.astrum.Astrum
import java.awt.Color

@Module
object BlockOverlay : Feature("overlayEnabled") {
    val outlineColor by config.property<Color>("blockHighlightColor")
    val outlineWidth by config.property<Int>("overlayLineWidth")
    val fillColor by config.property<Color>("blockFillColor")
    val fill by config.property<Boolean>("fillBlockOverlay")

    override fun initialize() {
        on<RenderEvent.World.BlockOutline> { event ->
            val blockPos = event.blockPos.toVec3()
            val blockShape = event.voxelShape
            if (blockShape.isEmpty) return@on
            event.cancel()

            Astrum.queueVoxelOutline(blockShape, blockPos, outlineColor, true, outlineWidth.toFloat())
            if (fill) Astrum.queueVoxelFill(blockShape, blockPos,fillColor, true)
        }
    }
}