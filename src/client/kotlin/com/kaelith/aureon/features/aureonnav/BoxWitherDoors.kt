package com.kaelith.aureon.features.aureonnav

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.map.Door
import com.kaelith.aureon.api.dungeons.utils.Checkmark
import com.kaelith.aureon.api.dungeons.utils.DoorState
import com.kaelith.aureon.api.dungeons.utils.DoorType
import com.kaelith.aureon.api.dungeons.utils.RoomType
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color
import kotlin.math.max

@Module
object BoxWitherDoors : Feature("boxWitherDoors", island = SkyBlockIsland.THE_CATACOMBS) {
    private var witherKeys = 0
    private var bloodKey = false
    private var bloodOpen = false

    private val openedDoor = Regex("""^(\w+) opened a WITHER door!$""")
    private val witherKey = Regex("""^.*?(\w+) has obtained Wither Key!$""")
    private val bloodKeyRegex = Regex("""^.*?(\w+) has obtained Blood Key!$""")
    private val bloodOpened = Regex("""^The BLOOD DOOR has been opened!$""")

    private val noKey by config.property<Color>("noKeyColor")
    private val noKeyFill by config.property<Color>("noKeyFillColor")
    private val key by config.property<Color>("keyColor")
    private val keyFill by config.property<Color>("keyFillColor")
    private val bloodColor by config.property<Color>("bloodBoxDoorColor")
    private val bloodFill by config.property<Color>("bloodDoorFillColor")
    private val normalColor by config.property<Color>("normalBoxDoorColor")
    private val normalFill by config.property<Color>("normalDoorFillColor")
    private val renderNormalDoors by config.property<Boolean>("renderNormalDoorBoxes")
    private val hideUselessNormalDoors by config.property<Boolean>("hideGreenNormalDoorBoxes")
    private val fillPhase by config.property<Boolean>("doorFillPhase")
    private val doorLW by config.property<Int>("doorLineWidth")

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            when {
                msg == "A Wither Key was picked up!" || witherKey.containsMatchIn(msg) -> witherKeys++
                msg == "A Blood Key was picked up!" || bloodKeyRegex.containsMatchIn(msg) -> bloodKey = true
                openedDoor.containsMatchIn(msg) -> witherKeys = max(0, witherKeys - 1)
                bloodOpened.containsMatchIn(msg) -> {
                    bloodKey = false
                    bloodOpen = true
                }
            }
        }

        on<DungeonEvent.KeyPickUp> { event ->
            val keyName = event.key.toString().uppercase()
            if ("BLOOD" in keyName) bloodKey = true else witherKeys++
        }

        on<RenderEvent.World.Last> {
            Dungeon.doors.forEach { door ->
                if (door == null || door.state != DoorState.DISCOVERED) return@forEach
                if (door.opened) return@forEach

                when (door.type) {
                    DoorType.WITHER -> drawDoor(door, if (witherKeys > 0) key else noKey, if (witherKeys > 0) keyFill else noKeyFill)
                    DoorType.BLOOD -> if (!bloodOpen) drawDoor(door, if (bloodKey) key else bloodColor, if (bloodKey) keyFill else bloodFill)
                    DoorType.NORMAL, DoorType.ENTRANCE -> {
                        if (!renderNormalDoors) return@forEach
                        if (hideUselessNormalDoors && !isUsefulNormalDoor(door)) return@forEach
                        drawDoor(door, normalColor, normalFill)
                    }
                }
            }
        }

        on<LocationEvent.ServerChange> { resetKeys() }
    }

    private fun drawDoor(door: Door, outline: Color, fill: Color) {
        val (x, y, z) = door.getPos()
        Render3D.drawBox(x.toDouble(), y.toDouble(), z.toDouble(), 3.0, 4.0, outline, depth = false, lineWidth = doorLW.toFloat())
        Render3D.drawFilledBox(x.toDouble(), y.toDouble(), z.toDouble(), 3.0, 4.0, fill, depth = !fillPhase)
    }

    private fun isUsefulNormalDoor(door: Door): Boolean {
        val candidates = door.getCandidates().mapNotNull { Dungeon.getRoomAtIdx(it) }
        if (candidates.isEmpty()) return true
        return candidates.any { room ->
            room.type != RoomType.ENTRANCE && room.checkmark != Checkmark.GREEN
        }
    }

    private fun resetKeys() {
        witherKeys = 0
        bloodKey = false
        bloodOpen = false
    }

    override fun onUnregister() = resetKeys()
}
