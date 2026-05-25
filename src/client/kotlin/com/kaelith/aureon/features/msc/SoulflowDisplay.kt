package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.PacketEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object SoulflowDisplay: Feature("soulflowDisplay", true) {
    private const val INTERNALIZED_PREFIX = "Internalized: "
    private val SOULFLOW_IDS = setOf("SOULFLOW_PILE", "SOULFLOW_BATTERY", "SOULFLOW_SUPERCELL")

    private const val displayName = "soulflowDisplay"
    private var soulflow = ""

    override fun initialize() {
        HUDManager.register(displayName, "§3500⸎ Soulflow", "soulflowDisplay")

        on<PacketEvent.Received> { event ->
            if (event.packet !is ClientboundContainerSetSlotPacket) return@on
            if (event.packet.item.getSkyBlockId() !in SOULFLOW_IDS) return@on
            soulflow = event.packet.item.getLore().firstOrNull { it.stripped.startsWith(INTERNALIZED_PREFIX) }?.stripped?.removePrefix(INTERNALIZED_PREFIX)?.takeIf { it.isNotBlank() } ?: ""
        }

        on<GuiEvent.RenderHUD> { render(it.context) }
    }

    private fun render(context: GuiGraphicsExtractor) = HUDManager.renderHud(displayName, context) { Render2D.drawString(context, "§3$soulflow", 0, 0) }
}