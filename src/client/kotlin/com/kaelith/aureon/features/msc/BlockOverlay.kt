package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.astrum.Astrum
import com.kaelith.aureon.api.orbit.Orbit.toVec3
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.world.phys.EntityHitResult
import java.awt.Color

@Module
object BlockOverlay : Feature("overlayEnabled") {
    private val outlineColor by config.property<Color>("blockHighlightColor")
    private val outlineWidth by config.property<Int>("overlayLineWidth")
    private val fillColor by config.property<Color>("blockFillColor")
    private val fill by config.property<Boolean>("fillBlockOverlay")
    private val entityHighlight by config.property<Boolean>("overlayEntityHighlight")
    private val outlineThroughWalls by config.property<Boolean>("overlayOutlinePhase")
    private val fillThroughWalls by config.property<Boolean>("overlayFillPhase")
    private val dynamicPhase by config.property<Boolean>("overlayDynamicPhase")

    override fun initialize() {
        on<RenderEvent.World.BlockOutline> { event ->
            val blockShape = event.voxelShape
            if (blockShape.isEmpty) return@on
            event.cancel()

            val blockPos = event.blockPos.toVec3()
            val firstPerson = client.options.cameraType.isFirstPerson
            val outlineDepth = !if (dynamicPhase) firstPerson else outlineThroughWalls
            val fillDepth = !if (dynamicPhase) firstPerson else fillThroughWalls

            Astrum.queueVoxelOutline(blockShape, blockPos, outlineColor, outlineDepth, outlineWidth.toFloat())
            if (fill) Astrum.queueVoxelFill(blockShape, blockPos, fillColor, fillDepth)
        }

        on<RenderEvent.World.Last> {
            if (!entityHighlight) return@on
            val entity = (client.hitResult as? EntityHitResult)?.entity ?: return@on
            val firstPerson = client.options.cameraType.isFirstPerson
            val outlineDepth = !if (dynamicPhase) firstPerson else outlineThroughWalls
            val fillDepth = !if (dynamicPhase) firstPerson else fillThroughWalls

            Render3D.drawEntityBox(entity, outlineColor, outlineDepth, outlineWidth.toFloat(), expand = 0.002)
            if (fill) Render3D.drawFilledEntityBox(entity, fillColor, fillDepth, expand = 0.002)
        }
    }
}
