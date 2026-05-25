package com.kaelith.aureon.features.msc

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.TablistEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.api.handlers.Capsule
import com.kaelith.aureon.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object PetDisplay: Feature("petDisplay", true) {
    const val NAME = "petDisplay"

    val petSummon = Regex("""You (summoned|despawned) your ([A-Za-z ]+)(?: ✦)?!""")
    val autoPet = Regex("""Autopet equipped your \[Lvl (\d+)] ([A-Za-z ]+)(?: ✦)?! VIEW RULE""")
    val tab = Regex("""\[Lvl (\d+)] ([A-Za-z ]+)(?: ✦)?""")

    var activePet: String? = null
    var activePetLvl = 0

    data class PetCache(
        val levels: MutableMap<String, Int> = mutableMapOf(),
        var lastActiveName: String? = null,
        var lastActiveLevel: Int = 0
    )

    val petCache = Capsule("petCache", PetCache())

    fun cachePet(petName: String, level: Int) {
        petCache.update {
            val current = levels[petName]
            if (current == null || level != current) {
                levels[petName] = level
                //AureonCore.LOGGER.info("Cached pet: $petName → Lvl $level")
            }
            // also update last active
            lastActiveName = petName
            lastActiveLevel = level
        }
    }

    fun getCachedLevel(petName: String): Int? = petCache().levels[petName]
    fun getAllCachedPets(): Map<String, Int> = petCache().levels
    fun getLastActivePet(): Pair<String, Int>? {
        val cache = petCache()
        return cache.lastActiveName?.let { it to cache.lastActiveLevel }
    }


    override fun initialize() {
        HUDManager.registerCustom(NAME, 120, 30,this::hudEditorRender, "petDisplay")

        getLastActivePet()?.let { (name, lvl) ->
            activePet = name
            activePetLvl = lvl
            //AureonCore.LOGGER.info("Restored last active pet from cache: $name Lvl $lvl")
        }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            // Pet summon/despawn matcher
            val summonMatch = petSummon.find(msg)
            if (summonMatch != null) {
                val action = summonMatch.groupValues[1] // "summoned" or "despawned"
                val petName = summonMatch.groupValues[2].trim()
                //AureonCore.LOGGER.info("Pet $action: $petName")

                when (action) {
                    "summoned" -> {
                        activePet = petName
                        activePetLvl = getCachedLevel(petName) ?: 0
                    }
                    "despawned" -> {
                        activePet = null
                        activePetLvl = 0
                    }
                }


                return@on
            }

            // Autopet matcher
            val autoMatch = autoPet.find(msg)
            if (autoMatch != null) {
                val level = autoMatch.groupValues[1].toInt()
                val petName = autoMatch.groupValues[2].trim()
                //AureonCore.LOGGER.info("Autopet equipped: Lvl $level $petName")
                activePet = petName
                activePetLvl = level
                cachePet(petName, level)

                return@on
            }
        }

        on<TablistEvent.Change> { tabEvent ->
            tabEvent.new.flatten().forEach{ entry ->
                val text = entry.stripped

                val tabMatch = tab.find(text)
                if (tabMatch != null) {
                    val level = tabMatch.groupValues[1].toInt()
                    val petName = tabMatch.groupValues[2].trim()
                    //AureonCore.LOGGER.info("tablist equipped: Lvl $level $petName")
                    activePet = petName
                    activePetLvl = level
                    cachePet(petName, level)

                }
            }
        }

        on<GuiEvent.RenderHUD> { event ->
            renderHud(event.context)
        }
    }

    fun hudEditorRender(
        context: GuiGraphicsExtractor,
    ){
        Render2D.drawString(context,"§bEnder Dragon", 40, 7)
        Render2D.drawString(context,"§7[Lvl 100]", 40, 17)
        val stack = RepoPetsAPI.getPetAsItem("ENDER_DRAGON", SkyBlockRarity.LEGENDARY)

        Render2D.renderItem(context,stack, 0f, -5f, 2.3f)
    }

    fun renderHud(context: GuiGraphicsExtractor) {
        if (activePet == null) return
        val matrix = context.pose()

        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        matrix.pushMatrix()
        matrix.translate(x, y)
        matrix.scale(scale, scale)

        Render2D.drawString(context,"§b$activePet", 40, 7)
        Render2D.drawString(context,"§7[Lvl $activePetLvl]", 40, 17)

        val stack = RepoPetsAPI.getPetAsItem(activePet?.replace(" ", "_").orEmpty().uppercase(), SkyBlockRarity.LEGENDARY)
        Render2D.renderItem(context,stack, 0f, -5f, 2.3f)

        matrix.popMatrix()
    }

}