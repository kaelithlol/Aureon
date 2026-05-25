package com.kaelith.aureon.features.secrets

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.features.secrets.utils.routes.RoutePlayer
import com.kaelith.aureon.features.secrets.utils.routes.RouteRecorder
import com.kaelith.aureon.features.secrets.utils.routes.RouteRegistry
import com.kaelith.aureon.features.secrets.utils.routes.StepData
import com.kaelith.aureon.features.secrets.utils.routes.WaypointData
import com.kaelith.aureon.features.secrets.utils.routes.WaypointType
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.Utils
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.api.config.core.Keybind
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.map.Room
import com.kaelith.aureon.api.dungeons.utils.Checkmark
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.secrets.utils.waypoints.SecretData
import com.kaelith.aureon.features.secrets.utils.waypoints.SecretsRegistry
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color

@Module
object SecretRoutes: Feature("secretRoutes", island = SkyBlockIsland.THE_CATACOMBS) {
    val onlyRenderAfterClear by config.property<Boolean>("secretRoutes.onlyRenderAfterClear")
    val stopRenderAfterGreen by config.property<Boolean>("secretRoutes.stopRenderAfterGreen")
    val nextStepBind by config.property<Keybind.Handler>("secretRoutes.nextStep")
    val lastStepBind by config.property<Keybind.Handler>("secretRoutes.lastStep")
    var routeFile by config.property<String>("secretRoutes.fileName")

    val startColor by config.property<Color>("secretRoutes.startColor")
    val mineColor by config.property<Color>("secretRoutes.mineColor")
    val superBoomColor by config.property<Color>("secretRoutes.superboomColor")
    val etherWarpColor by config.property<Color>("secretRoutes.etherwarpColor")
    val pearlColor by config.property<Color>("secretRoutes.pearlColor")
    val chestColor by config.property<Color>("secretRoutes.chestColor")
    val itemColor by config.property<Color>("secretRoutes.itemColor")
    val essenceColor by config.property<Color>("secretRoutes.essenceColor")
    val batColor by config.property<Color>("secretRoutes.batColor")
    val leverColor by config.property<Color>("secretRoutes.leverColor")

    val recordingHud by config.property<Boolean>("secretRoutes.recordingHud")
    val minimized by config.property<Boolean>("secretRoutes.recordingHud.minimized")
    val rHudName = "rhud"

    var lockedChest = false

    private var stepIndex = 0
    private var route: List<StepData> = emptyList()
    private var currentRoom: Room? = null
    private val currentStep: StepData? get() = route.getOrNull(stepIndex)
    private val firstStep: Boolean get() = route.indexOf(currentStep) == 0
    private val currentSecret: WaypointData? get() = currentStep?.waypoints?.firstOrNull() { it.type in WaypointType.SECRET }

    init {
        HUDManager.registerCustom(rHudName, 100, 110, RouteRecorder::hudPreview, "secretRoutes.recordingHud")
        on<GuiEvent.RenderHUD> { if (recordingHud) RouteRecorder.hud(it.context) }

        on<DungeonEvent.Secrets.Chest> { event ->
            val secPos = currentRoom?.getRealCoord(currentSecret?.pos ?: return@on) ?: return@on
            if ( secPos == event.blockPos) nextStep()
        }

        on<DungeonEvent.Secrets.Essence> { event ->
            val secPos = currentRoom?.getRealCoord(currentSecret?.pos ?: return@on) ?: return@on
            if ( secPos == event.blockPos) nextStep()
        }

        on<DungeonEvent.Secrets.Item> { event ->
            val secPos = currentRoom?.getRealCoord(currentSecret?.pos ?: return@on) ?: return@on
            val pos = world?.getEntity(event.entityId)?.blockPosition() ?: return@on
            if (Utils.calcDistance(pos, secPos) < 25) nextStep()
        }

        on<DungeonEvent.Secrets.Bat> { event ->
            val secPos = currentRoom?.getRealCoord(currentSecret?.pos ?: return@on) ?: return@on
            if (Utils.calcDistance(event.blockPos, secPos) < 100) nextStep()
        }

        on<DungeonEvent.Secrets.Misc> { event ->
            if (event.secretType != DungeonEvent.Secrets.Type.LEVER || !lockedChest) return@on
            lockedChest = false
        }

        on<DungeonEvent.Room.Change> { event ->
            currentRoom = event.new
            stepIndex = 0
            lockedChest = false
            route = RouteRegistry.getRoute(currentRoom?.name ?: return@on) ?: emptyList()
        }

        on<ChatEvent.Receive> { event ->
            if (event.message.stripped.lowercase() != "that chest is locked!") return@on
            lockedChest = true
            previousStep()
        }

        on<RenderEvent.World.Last> {
            if (currentRoom == null || route.isEmpty() || currentStep == null) return@on
            if (onlyRenderAfterClear && currentRoom?.checkmark in setOf(Checkmark.NONE, Checkmark.UNEXPLORED, Checkmark.UNDISCOVERED)) return@on
            if (stopRenderAfterGreen && currentRoom?.checkmark == Checkmark.GREEN) return@on
            RoutePlayer.renderRoute(currentStep!!, firstStep)

            if (Dungeon.inBoss || !lockedChest) return@on
            val data = SecretsRegistry.getById(currentRoom?.id ?: -1) ?: return@on
            val levers = data.toWaypoints(config, currentRoom!!, SecretData.Type.LEVER)
            levers.forEach { it.render() }
        }

        nextStepBind.onPress {
            if(!this@SecretRoutes.isEnabled() || RouteRecorder.recording) return@onPress
            nextStep()
        }

        lastStepBind.onPress{
            if(!this@SecretRoutes.isEnabled() || RouteRecorder.recording) return@onPress
            previousStep()
        }
    }

    fun nextStep() {
        if(stepIndex < route.size - 1) stepIndex++
    }

    fun previousStep() {
        if (stepIndex > 0) stepIndex--
    }
}
