package com.kaelith.aureon.features.aureonnav

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.zenith.textureManager
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.features.aureonnav.render.MapRenderer
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.mojang.blaze3d.platform.NativeImage
import net.fabricmc.loader.api.FabricLoader
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import java.awt.Color
import java.nio.file.Files

@Module
object Map: Feature("mapEnabled", island = SkyBlockIsland.THE_CATACOMBS) {
    private val TEXTURE_CACHE = mutableMapOf<String, Identifier>()
    private const val name = "AureonNav"

    // textures
    val SELF_MARKER by lazy { getOrLoad("markerself") }
    val OTHER_MARKER by lazy { getOrLoad( "markerother") }
    val DEFAULT_MAP by lazy { getOrLoad( "defaultmap") }

    // main map configs
    val bossMapEnabled by config.property<Boolean>("bossMapEnabled")
    val hideInBoss by config.property<Boolean>("hideInBoss")
    val scoreMapEnabled by config.property<Boolean>("scoreMapEnabled")
    val mapInfoUnder by config.property<Boolean>("mapInfoUnder")

    // display
    val mapBgColor by config.property<Color>("mapBgColor")
    val mapBorder by config.property<Boolean>("mapBorder")
    val mapBdColor by config.property<Color>("mapBdColor")
    val mapBdWidth by config.property<Int>("mapBdWidth")
    val nameScale by config.property<Float>("nameScale")
    val secretScale by config.property<Float>("secretScale")
    val checkmarkScale by config.property<Float>("checkmarkScale")
    val textShadow by config.property<Boolean>("mtextshadow")

    // behavior
    val roomCheck by config.property<Boolean>("roomCheck")
    val roomName by config.property<Boolean>("roomName")
    val roomSecrets by config.property<Boolean>("roomSecrets")

    val puzzleCheck by config.property<Boolean>("puzzleCheck")
    val puzzleName by config.property<Boolean>("puzzleName")
    val puzzleSecrets by config.property<Boolean>("puzzleSecrets")

    val checkAnchor by config.property<Int>("checkAnchor")
    val nameAnchor by config.property<Int>("nameAnchor")
    val secretsAnchor by config.property<Int>("secretsAnchor")

    val prioMiddle by config.property<Boolean>("prioMiddle")
    val replaceText by config.property<Boolean>("replaceText")

    // map colors
    val NormalColor by config.property<Color>("normalRoomColor")
    val PuzzleColor by config.property<Color>("puzzleRoomColor")
    val TrapColor by config.property<Color>("trapRoomColor")
    val MinibossColor by config.property<Color>("minibossRoomColor")
    val BloodColor by config.property<Color>("bloodRoomColor")
    val FairyColor by config.property<Color>("fairyRoomColor")
    val EntranceColor by config.property<Color>("entranceRoomColor")

    val NormalDoorColor by config.property<Color>("normalDoorColor")
    val WitherDoorColor by config.property<Color>("witherDoorColor")
    val BloodDoorColor by config.property<Color>("bloodDoorColor")
    val EntranceDoorColor by config.property<Color>("entranceDoorColor")

    // icon settings
    val iconScale by config.property<Float>("iconScale")
    val smoothMovement by config.property<Boolean>("smoothMovement")
    val showPlayerHead by config.property<Boolean>("showPlayerHeads")
    val ownDefault by config.property<Boolean>("ownDefault")
    val iconBorderWidth by config.property<Float>("iconBorderWidth")
    val iconBorderColor by config.property<Color>("iconBorderColor")
    val iconClassColors by config.property<Boolean>("iconClassColors")
    val showNames by config.property<Boolean>("showNames")
    val dontShowOwn by config.property<Boolean>("dontShowOwn")

    // other
    val hiddenRooms = false
    val tint = 0.7

    override fun initialize() {
        HUDManager.registerCustom(name, 148, 148, this::hudEditorRender, "mapEnabled")

        on<GuiEvent.RenderHUD> { event ->
            renderMap(event.context)
        }
    }

    fun hudEditorRender(context: GuiGraphicsExtractor){
        MapRenderer.renderPreview(context, 5f, 5f, 1f)
    }

    fun renderMap(context: GuiGraphicsExtractor) {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        MapRenderer.render(context, x, y, scale)
    }

    fun getOrLoad(fileName: String): Identifier? {
        if (TEXTURE_CACHE.containsKey(fileName)) return TEXTURE_CACHE[fileName]

        val path = FabricLoader.getInstance().configDir.resolve("Aureon/assets/$fileName.png")
        if (!Files.exists(path)) return null

        try {
            Files.newInputStream(path).use { inputStream ->
                val nativeImage = NativeImage.read(inputStream)
                val dynamicTexture = DynamicTexture({ "Aureon Dynamic: $fileName" }, nativeImage)
                val loc = Identifier.fromNamespaceAndPath(AureonCore.NAMESPACE, "aureonnav/${fileName.lowercase().replace(".", "_")}")
                textureManager.register(loc, dynamicTexture)
                TEXTURE_CACHE[fileName] = loc
                return loc
            }
        } catch (e: Exception) {
            AureonCore.LOGGER.error("Failed to load dynamic texture: $fileName", e)
            return null
        }
    }
}
