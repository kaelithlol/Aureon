package com.kaelith.aureon.features.aureonnav.render

import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.api.zenith.world
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.state.MapRenderState
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.MapItem
import tech.thatgravyboat.skyblockapi.platform.pushPop

object Score {
    private val state = MapRenderState()

    fun render(context: GuiGraphicsExtractor) {
        updateState()
        if (state.texture == null) return

        context.pushPop {
            context.pose().translate(5f, 5f)
            context.map(state)
        }
    }

    private fun updateState() {
        val p = player ?: return
        val w = world ?: return
        val stack = p.inventory.getItem(8)

        if (stack.item is MapItem) {
            val id = stack.get(DataComponents.MAP_ID) ?: return
            val data = MapItem.getSavedData(id, w) ?: return
            client.mapRenderer.extractRenderState(id, data, state)
        }
    }
}
