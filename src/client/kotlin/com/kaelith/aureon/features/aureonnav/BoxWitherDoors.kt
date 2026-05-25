package com.kaelith.aureon.features.aureonnav

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.render.Render3D
import com.kaelith.aureon.api.dungeons.utils.DoorState
import com.kaelith.aureon.api.dungeons.utils.DoorType
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.utils.config
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color

@Module
object BoxWitherDoors: Feature("boxWitherDoors", island = SkyBlockIsland.THE_CATACOMBS) {
    var keyObtained = false
    var bloodOpen = false

    val openedDoor = Regex("""^(\w+) opened a WITHER door!$""")
    val bloodOpened = Regex("""^The BLOOD DOOR has been opened!$""")

    val noKey by config.property<Color>("noKeyColor")
    val key by config.property<Color>("keyColor")
    val doorLW by config.property<Int>("doorLineWidth")

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            val doorMatch = openedDoor.find(msg)
            if (doorMatch != null){
                keyObtained = false
                return@on
            }

            val bloodMatch = bloodOpened.find(msg)
            if (bloodMatch != null){
                keyObtained = false
                bloodOpen = true
                return@on
            }
        }

        on<DungeonEvent.KeyPickUp> {
            keyObtained = true
        }

        on<RenderEvent.World.Last> {
            if(bloodOpen) return@on

            val color = if (keyObtained) key else noKey

            Dungeon.doors.forEach { door ->
                if (door == null || door.opened) return@forEach
                if (door.state != DoorState.DISCOVERED) return@forEach
                if (door.type !in setOf(DoorType.WITHER, DoorType.BLOOD)) return@forEach

                val (x, y, z) = door.getPos()

                Render3D.drawBox(
                    x.toDouble(), y.toDouble(), z.toDouble(),
                    3.0, 4.0,
                    color, false, doorLW.toFloat()
                )
            }
        }

        on<LocationEvent.ServerChange>{
            bloodOpen = false
            keyObtained = false
        }
    }

    override fun onUnregister() {
        bloodOpen = false
        keyObtained = false
    }
}
