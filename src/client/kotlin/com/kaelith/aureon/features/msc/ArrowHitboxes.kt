package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import java.awt.Color

@Module
object ArrowHitboxes : Feature("arrowHitboxes") {
    private val outlineColor by config.property<Color>("arrowHitboxOutlineColor")
    private val fillColor by config.property<Color>("arrowHitboxFillColor")
    private val lineWidth by config.property<Int>("arrowHitboxLineWidth")
    private val throughWalls by config.property<Boolean>("arrowHitboxPhase")

    override fun initialize() {
        on<RenderEvent.World.Last> {
            val level = world ?: return@on
            val depth = !throughWalls

            level.entitiesForRendering().forEach { entity ->
                if (entity !is AbstractArrow || entity.isRemoved) return@forEach
                Render3D.drawEntityBox(entity, outlineColor, depth, lineWidth.toFloat(), expand = 0.002)
                Render3D.drawFilledEntityBox(entity, fillColor, depth, expand = 0.002)
            }
        }
    }
}
