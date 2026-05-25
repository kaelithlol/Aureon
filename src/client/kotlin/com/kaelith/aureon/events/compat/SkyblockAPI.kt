package com.kaelith.aureon.events.compat

import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.info.ScoreboardTitleUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.info.ScoreboardUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.info.TabListChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.PlayerHotbarChangeEvent
import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.EventBus
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.PlayerEvent
import com.kaelith.aureon.events.core.RepoEvent
import com.kaelith.aureon.events.core.ScoreboardEvent
import com.kaelith.aureon.events.core.TablistEvent
import net.hypixel.data.type.GameType
import tech.thatgravyboat.repolib.api.RepoStatus
import tech.thatgravyboat.skyblockapi.api.events.dungeon.DungeonKeyPickedUpEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.AreaChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.IslandChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RepoStatusEvent

/**
 * Handles and converts SkyblockAPI events to our own.
 */
@Module
object SkyblockAPI {
    init {
        SkyBlockAPI.eventBus.register(this)
    }

    @Subscription
    fun onTabListUpdate(event: TabListChangeEvent) {
        EventBus.post(TablistEvent.Change(event.old, event.new))
    }

    @Subscription
    fun onScoreboardTitleUpdate(event: ScoreboardTitleUpdateEvent) {
        EventBus.post(ScoreboardEvent.UpdateTitle(event.old, event.new))
    }

    @Subscription
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        EventBus.post(ScoreboardEvent.Update(event.old, event.new, event.components))
    }

    @Subscription
    fun onPlayerHotbarUpdate(event: PlayerHotbarChangeEvent) {
        EventBus.post(PlayerEvent.HotbarChange(event.slot, event.item))
    }

    @Subscription
    fun onAriaChange(event: AreaChangeEvent) {
        EventBus.post(LocationEvent.AreaChange(event.old, event.new))
    }

    @Subscription
    fun onIslandChange(event: IslandChangeEvent) {
        EventBus.post(LocationEvent.IslandChange(event.old, event.new))
    }

    @Subscription
    fun onServerChange(event: ServerChangeEvent) {
        val isOnSkyBlock = event.type == GameType.SKYBLOCK
        if (isOnSkyBlock) EventBus.post(LocationEvent.SkyblockJoin())
        else EventBus.post(LocationEvent.SkyblockLeave())
    }

    @Subscription
    fun onKeyPickup(event: DungeonKeyPickedUpEvent) {
        EventBus.post(DungeonEvent.KeyPickUp(event.key))
    }

    @Subscription
    fun onRepoStatus(event: RepoStatusEvent) {
        EventBus.post(RepoEvent.Sataus(event.status))
        if (event.status == RepoStatus.SUCCESS) EventBus.post(RepoEvent.Success())
    }
}