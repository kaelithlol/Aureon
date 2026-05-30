package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import com.kaelith.aureon.utils.render.Render2D.width
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color

@Module
object CakeNumbers : Feature("cakeNumbers", skyblockOnly = true) {
    private val color by config.property<Color>("cakeNumbers.color")
    private val cakeYearRegex = """New Year Cake \(Year ([0-9]+)\)""".toRegex()

    override fun initialize() {
        on<GuiEvent.Container.Content> { event ->
            if (!LocationAPI.isOnSkyBlock) return@on
            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on

            for (slot in screen.menu.slots) {
                val stack = slot.item
                if (!stack.`is`(Items.CAKE)) continue
                val year = cakeYearRegex.find(stack.hoverName.stripped)?.groupValues?.get(1) ?: continue
                val x = event.x + slot.x + 8 - year.width() / 2
                val y = event.y + slot.y + 5
                Render2D.drawString(event.context, year, x, y, scale = 0.8f, color = color)
            }
        }
    }
}
