package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.zenith.camera
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import java.awt.Color

@Module
object BloodESP : Feature("bloodEsp", island = SkyBlockIsland.THE_CATACOMBS) {
    private val roomBox by config.property<Boolean>("bloodEsp.roomBox")
    private val tracer by config.property<Boolean>("bloodEsp.tracer")
    private val color by config.property<Color>("bloodEsp.color")
    private var bloodCenter: BlockPos? = null

    override fun initialize() {
        on<LocationEvent.ServerChange> { bloodCenter = null }
        on<DungeonEvent.Start> { bloodCenter = null }

        on<TickEvent.Client> {
            if (Dungeon.complete || Dungeon.bloodClear || Dungeon.bloodDone) return@on
            if (bloodCenter == null) bloodCenter = findBloodCenter()
        }

        on<RenderEvent.World.Last> {
            if (!roomBox && !tracer) return@on
            if (Dungeon.complete || Dungeon.bloodClear || Dungeon.bloodDone) return@on
            val center = bloodCenter ?: return@on

            if (roomBox) {
                Render3D.drawBox(center.x.toDouble(), 66.0, center.z.toDouble(), 31.0, 34.0, color, depth = false, lineWidth = 2f)
                Render3D.drawFilledBox(center.x.toDouble(), 66.0, center.z.toDouble(), 31.0, 34.0, Color(color.red, color.green, color.blue, 35), depth = false)
            }

            if (tracer) {
                Render3D.drawLine(camera.position(), Vec3(center.x + 0.5, 75.0, center.z + 0.5), 2f, color, depth = false)
            }
        }
    }

    private fun findBloodCenter(): BlockPos? {
        val level = world ?: return null
        val room = Dungeon.uniqueRooms.firstOrNull { it.name == "Blood" } ?: return null
        val (x, z) = room.realComponents.firstOrNull() ?: return null
        val center = BlockPos(x, 99, z)

        val offsets = listOf(
            -15 to -6,
            -6 to 15,
            15 to 6,
            6 to -15
        )

        return if (offsets.any { (dx, dz) -> level.getBlockState(center.offset(dx, 0, dz)).`is`(Blocks.REDSTONE_BLOCK) }) center else center
    }

    override fun onUnregister() {
        bloodCenter = null
    }
}
