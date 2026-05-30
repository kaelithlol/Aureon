package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.camera
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color

@Module
object DoorKeysESP : Feature("doorKeysEsp", island = SkyBlockIsland.THE_CATACOMBS) {
    private val highlightWither by config.property<Boolean>("doorKeysEsp.wither")
    private val highlightBlood by config.property<Boolean>("doorKeysEsp.blood")
    private val witherColor by config.property<Color>("doorKeysEsp.witherColor")
    private val bloodColor by config.property<Color>("doorKeysEsp.bloodColor")
    private var keyStand: ArmorStand? = null
    private var keyColor: Color? = null

    override fun initialize() {
        on<LocationEvent.ServerChange> { clear() }

        on<TickEvent.Client> {
            val current = keyStand
            if (current != null && current.isAlive) return@on
            clear()

            val level = world ?: return@on
            level.entitiesForRendering().filterIsInstance<ArmorStand>().forEach { stand ->
                when (stand.customName?.stripped) {
                    "Wither Key" -> if (highlightWither) remember(stand, witherColor)
                    "Blood Key" -> if (highlightBlood) remember(stand, bloodColor)
                }
            }
        }

        on<RenderEvent.World.Last> {
            val stand = keyStand ?: return@on
            val color = keyColor ?: return@on
            if (!stand.isAlive) {
                clear()
                return@on
            }

            val target = Vec3(stand.x, stand.y + 1.7, stand.z)
            Render3D.drawLine(camera.position(), target, 2f, color, depth = false)
            Render3D.drawBox(stand.x, stand.y + 1.15, stand.z, 0.8, 0.8, color, depth = false, lineWidth = 2f)
            Render3D.drawFilledBox(stand.x, stand.y + 1.15, stand.z, 0.8, 0.8, color, depth = false)
        }
    }

    private fun remember(stand: ArmorStand, color: Color) {
        keyStand = stand
        keyColor = color
    }

    private fun clear() {
        keyStand = null
        keyColor = null
    }

    override fun onUnregister() = clear()
}
