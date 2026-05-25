package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import java.awt.Color
import kotlin.math.absoluteValue

@Module
object StarMobBoxes : Feature("starMobBoxes", island = SkyBlockIsland.THE_CATACOMBS) {
    private val mobColor by config.property<Color>("starMobColor")
    private val chunkyColor by config.property<Color>("starMobChunkyColor")
    private val felColor by config.property<Color>("starMobFelColor")
    private val miniColor by config.property<Color>("starMobMiniColor")
    private val shadowAssassinColor by config.property<Color>("starMobShadowAssassinColor")
    private val skeletonMasterColor by config.property<Color>("starMobSkeletonMasterColor")
    private val lineWidth by config.property<Int>("starMobLineWidth")
    private val fillAlpha by config.property<Float>("starMobFillAlpha")
    private val showFullShadowAssassin by config.property<Boolean>("starMobShowFullShadowAssassin")
    private val throughWalls by config.property<Boolean>("starMobPhase")

    private val tracked = linkedMapOf<Int, MobData>()

    private data class MobData(
        val height: Double,
        val color: Color,
        val isFel: Boolean = false,
        val isShadowAssassin: Boolean = false,
    )

    override fun initialize() {
        on<TickEvent.Client> {
            val level = world ?: return@on

            tracked.entries.removeIf { (id, _) ->
                val entity = level.getEntity(id) as? LivingEntity
                entity == null || entity.isRemoved || entity.isDeadOrDying
            }

            level.entitiesForRendering().forEach { entity ->
                when (entity) {
                    is ArmorStand -> {
                        val name = entity.customName?.string ?: return@forEach
                        val data = getMobDataFromArmorStand(name) ?: return@forEach
                        val mobId = entity.id - if (name.contains("Withermancer", ignoreCase = true)) 3 else 1
                        val mob = level.getEntity(mobId) as? LivingEntity ?: return@forEach
                        if (mob is ArmorStand || mob.isRemoved || mob.isDeadOrDying) return@forEach
                        tracked[mob.id] = data
                    }

                    is LivingEntity -> {
                        val data = getMobDataFromEntityName(entity.name.string) ?: return@forEach
                        tracked[entity.id] = data
                    }
                }
            }
        }

        on<RenderEvent.World.Last> {
            val level = world ?: return@on
            val partialTick = client.deltaTracker.getGameTimeDeltaPartialTick(false)
            val depth = !throughWalls

            tracked.entries.removeIf { (id, data) ->
                val entity = level.getEntity(id) as? LivingEntity ?: return@removeIf true
                if (entity.isRemoved || entity.isDeadOrDying) return@removeIf true

                val pos = entity.getPosition(partialTick)
                val height = when {
                    data.isFel && entity.isInvisible -> 0.8
                    data.isShadowAssassin && !showFullShadowAssassin && entity.isInvisible -> 0.8
                    else -> data.height
                }
                val width = 0.8 + ((id % 97).absoluteValue / 10_000.0)
                val box = AABB(
                    pos.x - width / 2.0, pos.y, pos.z - width / 2.0,
                    pos.x + width / 2.0, pos.y + height, pos.z + width / 2.0,
                )

                Render3D.drawAABB(box, data.color, depth, lineWidth.toFloat())
                Render3D.drawFilledAABB(box, alpha(data.color, fillAlpha), depth = true)
                false
            }
        }

        on<LocationEvent.ServerChange> { clear() }
    }

    override fun onUnregister() = clear()

    private fun clear() {
        tracked.clear()
    }

    private fun getMobDataFromArmorStand(name: String): MobData? {
        val isStarred = name.indexOf('✯') >= 0 || name.indexOf('★') >= 0
        if (name.contains("Shadow Assassin", ignoreCase = true)) return MobData(2.0, shadowAssassinColor, isShadowAssassin = true)
        if (name.contains("Fels", ignoreCase = true)) return MobData(3.0, felColor, isFel = true)
        if (name.contains("Skeleton Master", ignoreCase = true)) return MobData(2.0, skeletonMasterColor)
        if (name.contains("Withermancer", ignoreCase = true)) return MobData(3.0, chunkyColor)
        if (
            name.contains("Lord", ignoreCase = true) ||
            name.contains("Zombie Commander", ignoreCase = true) ||
            name.contains("Super Archer", ignoreCase = true)
        ) return MobData(2.0, chunkyColor)
        if (
            name.contains("Adventurer", ignoreCase = true) ||
            name.contains("Angry Archaeologist", ignoreCase = true) ||
            name.contains("King Midas", ignoreCase = true)
        ) return MobData(2.0, miniColor)

        return if (isStarred) MobData(2.0, mobColor) else null
    }

    private fun getMobDataFromEntityName(name: String): MobData? = when (name) {
        "Shadow Assassin" -> MobData(2.0, shadowAssassinColor, isShadowAssassin = true)
        "Lost Adventurer", "Diamond Guy", "King Midas", "Angry Archaeologist" -> MobData(2.0, miniColor)
        else -> null
    }

    private fun alpha(color: Color, alpha: Float): Color {
        val a = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        return Color(color.red, color.green, color.blue, a)
    }
}
