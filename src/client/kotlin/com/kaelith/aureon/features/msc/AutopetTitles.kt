package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.Utils
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.substring

@Module
object AutopetTitles: Feature("autopetMessages", true) {
    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped
            val autoMatch = PetDisplay.autoPet.find(msg)?.groups[2] ?: return@on
            val formattedPetName = event.message.substring(autoMatch.range.first, autoMatch.range.last + 1)
            Utils.alert(Component.literal("§aEquipped: ").append(formattedPetName))
        }
    }
}