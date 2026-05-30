package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config

@Module
object AutoFriend : Feature("autoFriend") {
    private val hideFriendRequest by config.property<Boolean>("autoFriend.hideRequest")
    private val onlyOnHypixel by config.property<Boolean>("autoFriend.onlyOnHypixel")
    private val friendRequestPattern =
        Regex("""Friend request from (?<name>.+?)\s*\[ACCEPT] - \[DENY] - \[BLOCK].*""")
    private val rankPrefixPattern = Regex("""^\[[^\]]+]\s*""")

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@on
            if (onlyOnHypixel && !isOnHypixel()) return@on

            val message = event.message.string.replace("\n", "")
            if (": " in message) return@on

            val name = friendRequestPattern.find(message)
                ?.groups
                ?.get("name")
                ?.value
                ?.trim()
                ?.replace(rankPrefixPattern, "")
                ?.trim()
                ?: return@on

            if (name.isEmpty()) return@on

            Signal.sendCommand("friend $name")
            if (hideFriendRequest) event.cancel()
        }
    }

    fun toggle(): Boolean {
        enabled = !enabled
        config.save()
        return enabled
    }

    private fun isOnHypixel(): Boolean {
        val address = client.currentServer?.ip ?: return false
        return address.contains("hypixel", ignoreCase = true)
    }

    var enabled by config.property<Boolean>("autoFriend")
}
