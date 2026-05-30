package com.kaelith.aureon.generated

import com.kaelith.aureon.api.handlers.Atlas

object GeneratedFeatureRegistry {
    val modules: List<Class<*>> = listOf<Class<*>>(
        com.kaelith.aureon.events.EventBus::class.java,
        com.kaelith.aureon.managers.EventBusManager::class.java,
        com.kaelith.aureon.events.compat.SkyblockAPI::class.java,
        com.kaelith.aureon.api.hypixel.HypixelApi::class.java,
        com.kaelith.aureon.api.dungeons.Dungeon::class.java,
        com.kaelith.aureon.api.astrum.Astrum::class.java,
        com.kaelith.aureon.api.handlers.AutoUpdater::class.java,
        com.kaelith.aureon.api.handlers.Ether::class.java,
        com.kaelith.aureon.api.handlers.Pulsar::class.java,
        com.kaelith.aureon.features.aureonnav.BoxWitherDoors::class.java,
        com.kaelith.aureon.features.aureonnav.DungeonBreakdown::class.java,
        com.kaelith.aureon.features.aureonnav.Map::class.java,
        com.kaelith.aureon.features.aureonnav.MapInfo::class.java,
        com.kaelith.aureon.features.dungeons.CryptReminder::class.java,
        com.kaelith.aureon.features.dungeons.DungeonQoL::class.java,
        com.kaelith.aureon.features.dungeons.DungeonSplits::class.java,
        com.kaelith.aureon.features.dungeons.JoinInfo::class.java,
        com.kaelith.aureon.features.dungeons.LeapAnnounce::class.java,
        com.kaelith.aureon.features.dungeons.RoomName::class.java,
        com.kaelith.aureon.features.dungeons.ScoreAlerts::class.java,
        com.kaelith.aureon.features.dungeons.StarMobBoxes::class.java,
        com.kaelith.aureon.features.dungeons.TeammateMissing::class.java,
        com.kaelith.aureon.features.dungeons.TermNumbers::class.java,
        com.kaelith.aureon.features.dungeons.TermTracker::class.java,
        com.kaelith.aureon.features.msc.AutoFriend::class.java,
        com.kaelith.aureon.features.msc.ArrowHitboxes::class.java,
        com.kaelith.aureon.features.msc.AutopetTitles::class.java,
        com.kaelith.aureon.features.msc.Bars::class.java,
        com.kaelith.aureon.features.msc.BlockOverlay::class.java,
        com.kaelith.aureon.features.msc.Cosmetics::class.java,
        com.kaelith.aureon.features.msc.InventoryButtons::class.java,
        com.kaelith.aureon.features.msc.PetDisplay::class.java,
        com.kaelith.aureon.features.msc.SoulflowDisplay::class.java,
        com.kaelith.aureon.features.msc.SwordBlocking::class.java,
        com.kaelith.aureon.features.secrets.SecretRoutes::class.java,
        com.kaelith.aureon.features.secrets.SecretWaypoints::class.java,
        com.kaelith.aureon.features.secrets.utils.routes.RouteRecorder::class.java,
        com.kaelith.aureon.features.secrets.utils.routes.RouteRegistry::class.java
    )

    val commands: List<Atlas> = listOf(
        com.kaelith.aureon.utils.MainCommand,
        com.kaelith.aureon.features.dungeons.FunnyCommand,
        com.kaelith.aureon.features.msc.AutoFriendCommand
    )
}
