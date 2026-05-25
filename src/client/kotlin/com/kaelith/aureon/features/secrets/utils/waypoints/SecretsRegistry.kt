package com.kaelith.aureon.features.secrets.utils.waypoints

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.api.handlers.Quasar
import com.kaelith.aureon.events.EventBus
import com.kaelith.aureon.events.core.LocationEvent

object SecretsRegistry {
    private val byId = mutableMapOf<Int, SecretData>()
    private val allRooms = mutableListOf<SecretData>()
    private val ROOM_DATA_URL = "${AureonCore.ETHER}/secretCoords.json"

    init {
        EventBus.on<LocationEvent.IslandChange> { resetSecrets() }
    }

    fun load() {
        Quasar.fetch<List<SecretData>>(ROOM_DATA_URL) { result ->
            result.onSuccess { rooms ->
                populateRooms(rooms)
                AureonCore.LOGGER.info("SecretsRegistry: Loaded ${rooms.size} rooms from Ether")
            }

            result.onFailure {
                AureonCore.LOGGER.warn("SecretsRegistry: Failed to load local room data — ${it.message}")
            }
        }
    }

    fun populateRooms(rooms: List<SecretData>) {
        allRooms += rooms
        for (room in rooms) {
            byId[room.roomID] = room
        }
    }

    fun resetSecrets() {
        allRooms.forEach { room ->
            room.redstoneKey.forEach { it.collected = false }
            room.wither.forEach { it.collected = false }
            room.bat.forEach { it.collected = false }
            room.item.forEach { it.collected = false }
            room.chest.forEach { it.collected = false }
            room.lever.forEach { it.collected = false }
        }
    }

    fun getById(id: Int): SecretData? = byId[id]
    fun getAll(): List<SecretData> = allRooms
}