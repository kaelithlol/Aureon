package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.PacketEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color
import kotlin.math.roundToInt

@Module
object Floor7Helpers : Feature("floor7Helpers", island = SkyBlockIsland.THE_CATACOMBS) {
    private val witherEsp by config.property<Boolean>("floor7Helpers.witherEsp")
    private val witherColor by config.property<Color>("floor7Helpers.witherColor")
    private val crystalTimer by config.property<Boolean>("floor7Helpers.crystalTimer")
    private val crystalPlace by config.property<Boolean>("floor7Helpers.crystalPlace")
    private val relicTimer by config.property<Boolean>("floor7Helpers.relicTimer")
    private val relicBoxes by config.property<Boolean>("floor7Helpers.relicBoxes")

    private val crystalPickup = Regex("^(\\w+) picked up an Energy Crystal!$")
    private val crystalRespawn = Regex("^\\[BOSS] Maxor: (THAT BEAM! IT HURTS! IT HURTS!!|YOU TRICKED ME!)$")
    private val p5Start = Regex("^\\[BOSS] Necron: All this, for nothing\\.\\.\\.$")
    private var crystalPickupAt: Long? = null
    private var crystalTicks = 0
    private var relicTicks = 0

    override fun initialize() {
        on<LocationEvent.ServerChange> { reset() }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped
            if (!isF7Like()) return@on

            crystalPickup.find(msg)?.let { match ->
                if (crystalPlace && match.groupValues[1] == client.user.name) crystalPickupAt = System.currentTimeMillis()
            }

            if (crystalTimer && crystalRespawn.matches(msg)) crystalTicks = 34
            if (relicTimer && Dungeon.floor == DungeonFloor.M7 && p5Start.matches(msg)) relicTicks = 42
        }

        on<PacketEvent.ReceivedPost> { event ->
            if (!crystalPlace || crystalPickupAt == null) return@on
            val packet = event.packet as? ClientboundAddEntityPacket ?: return@on
            if (packet.type != EntityType.END_CRYSTAL || packet.y.toInt() != 224) return@on
            val self = player ?: return@on
            val dist = Vec3(packet.x, 0.0, packet.z).distanceTo(Vec3(self.x, 0.0, self.z))
            if (dist > 5.0) return@on
            val seconds = (System.currentTimeMillis() - (crystalPickupAt ?: return@on)) / 1000.0
            Signal.fakeMessage("${AureonCore.PREFIX} §aCrystal placed in §e${"%.3f".format(seconds)}s§a.")
            crystalPickupAt = null
        }

        on<TickEvent.Server> {
            if (crystalTicks > 0) crystalTicks--
            if (relicTicks > 0) relicTicks--
        }

        on<GuiEvent.RenderHUD> { event ->
            val x = event.context.guiWidth() / 2
            val y = event.context.guiHeight() / 2
            if (crystalTimer && crystalTicks > 0) Render2D.drawString(event.context, "§b${formatTicks(crystalTicks)}", x - 18, y + 10, 2.5f)
            if (relicTimer && relicTicks > 0) Render2D.drawString(event.context, "§d${formatTicks(relicTicks)}", x - 18, y + 35, 2.5f)
        }

        on<RenderEvent.World.Last> {
            if (witherEsp && isF7Like() && Dungeon.inBoss) renderWithers()
            if (relicBoxes && Dungeon.floor == DungeonFloor.M7 && Dungeon.inBoss) renderHeldRelic()
        }
    }

    private fun isF7Like(): Boolean = Dungeon.floor == DungeonFloor.F7 || Dungeon.floor == DungeonFloor.M7

    private fun renderWithers() {
        val level = world ?: return
        level.entitiesForRendering().filterIsInstance<WitherBoss>().forEach { wither ->
            if (wither.isInvisible || wither.invulnerableTicks == 800) return@forEach
            Render3D.drawEntityBox(wither, witherColor, depth = false, lineWidth = 2f, expand = 0.4)
        }
    }

    private fun renderHeldRelic() {
        val heldId = player?.mainHandItem?.getSkyBlockId() ?: return
        val relic = relics[heldId] ?: return
        Render3D.outlineBlock(relic.cauldron, relic.color, lineWidth = 3f, depth = false)
        Render3D.fillBlock(relic.cauldron, Color(relic.color.red, relic.color.green, relic.color.blue, 60), depth = false)
    }

    private fun formatTicks(ticks: Int): String {
        val seconds = (ticks / 20.0 * 100.0).roundToInt() / 100.0
        return "%.2f".format(seconds)
    }

    private fun reset() {
        crystalPickupAt = null
        crystalTicks = 0
        relicTicks = 0
    }

    private data class Relic(val cauldron: BlockPos, val color: Color)

    private val relics = mapOf(
        "CORRUPTED_RED_RELIC" to Relic(BlockPos(51, 7, 42), Color(255, 80, 80, 180)),
        "CORRUPTED_ORANGE_RELIC" to Relic(BlockPos(57, 7, 42), Color(255, 170, 0, 180)),
        "CORRUPTED_BLUE_RELIC" to Relic(BlockPos(59, 7, 44), Color(80, 140, 255, 180)),
        "CORRUPTED_GREEN_RELIC" to Relic(BlockPos(49, 7, 44), Color(80, 255, 110, 180)),
        "CORRUPTED_PURPLE_RELIC" to Relic(BlockPos(54, 7, 41), Color(180, 80, 255, 180))
    )
}
