package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.zenith.world
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.RenderEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render3D
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Blaze
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.awt.Color
import kotlin.math.floor

@Module
object PuzzleSolvers : Feature("puzzleSolvers", island = SkyBlockIsland.THE_CATACOMBS) {
    private val quiz by config.property<Boolean>("puzzleSolvers.quiz")
    private val quizColor by config.property<Color>("puzzleSolvers.quizColor")
    private val threeWeirdos by config.property<Boolean>("puzzleSolvers.threeWeirdos")
    private val weirdosCorrectColor by config.property<Color>("puzzleSolvers.weirdosCorrectColor")
    private val weirdosWrongColor by config.property<Color>("puzzleSolvers.weirdosWrongColor")
    private val blaze by config.property<Boolean>("puzzleSolvers.blaze")
    private val blazeFirstColor by config.property<Color>("puzzleSolvers.blazeFirstColor")
    private val blazeSecondColor by config.property<Color>("puzzleSolvers.blazeSecondColor")
    private val blazeOtherColor by config.property<Color>("puzzleSolvers.blazeOtherColor")

    private val blazeHpRegex = Regex("^\\[Lv15].*Blaze [\\d,]+/([\\d,]+).*$")
    private val npcRegex = Regex("^\\[NPC] (\\w+): (.+)$")
    private val optionRegex = Regex("^[ⒶⒷⒸⓐⓑⓒ]\\s+(.+)$")

    private val blazes = mutableListOf<Entity>()
    private val blazeHealth = mutableMapOf<Int, Int>()
    private val wrongWeirdoChests = mutableSetOf<BlockPos>()
    private var correctWeirdoChest: BlockPos? = null
    private val quizAnswerBlocks = mutableSetOf<BlockPos>()
    private var expectedQuizAnswers: List<String> = emptyList()

    override fun initialize() {
        on<DungeonEvent.Room.Change> { resetRoomState() }
        on<DungeonEvent.Room.StateChange> {
            if (it.room.name == "Three Weirdos") resetWeirdos()
        }

        on<TickEvent.Client> {
            if (blaze && Dungeon.currentRoom?.name?.contains("Blaze", ignoreCase = true) == true) updateBlazes()
            else blazes.clear()
        }

        on<ChatEvent.Receive> { event ->
            val message = event.message.stripped
            if (quiz) handleQuizChat(message)
            if (threeWeirdos) handleThreeWeirdosChat(message)
        }

        on<RenderEvent.World.Last> {
            if (quiz) quizAnswerBlocks.forEach { pos -> renderBlock(pos, quizColor) }
            if (threeWeirdos) {
                correctWeirdoChest?.let { renderBlock(it, weirdosCorrectColor) }
                wrongWeirdoChests.forEach { renderBlock(it, weirdosWrongColor) }
            }
            if (blaze) renderBlazes()
        }
    }

    private fun handleQuizChat(message: String) {
        if (Dungeon.currentRoom?.name != "Quiz") return
        val trimmed = message.trim()

        quizSolutions.entries.firstOrNull { (question, _) -> trimmed.contains(question, ignoreCase = true) }?.let { (_, answers) ->
            expectedQuizAnswers = answers
            quizAnswerBlocks.clear()
            return
        }

        if (trimmed == "What SkyBlock year is it?") {
            val year = (((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1
            expectedQuizAnswers = listOf("Year $year")
            quizAnswerBlocks.clear()
            return
        }

        val answer = optionRegex.find(trimmed)?.groupValues?.getOrNull(1) ?: return
        if (expectedQuizAnswers.none { answer.endsWith(it, ignoreCase = true) }) return

        val index = when (trimmed.first()) {
            'Ⓐ', 'ⓐ' -> 0
            'Ⓑ', 'ⓑ' -> 1
            'Ⓒ', 'ⓒ' -> 2
            else -> return
        }

        quizOptionPos(index)?.let { quizAnswerBlocks += it }
    }

    private fun quizOptionPos(index: Int): BlockPos? {
        val room = Dungeon.currentRoom ?: return null
        if (room.name != "Quiz") return null
        if (room.corner == null || room.rotation == null) room.findRotation()
        val local = when (index) {
            0 -> BlockPos(5, 70, -9)
            1 -> BlockPos(0, 70, -6)
            else -> BlockPos(-5, 70, -9)
        }
        return room.getRealCoord(local)
    }

    private fun handleThreeWeirdosChat(message: String) {
        if (Dungeon.currentRoom?.name != "Three Weirdos") return
        val (npcName, text) = npcRegex.find(message)?.destructured ?: return
        val solution = weirdoSolutions.any { it.matches(text) }
        val wrong = weirdoWrong.any { it.matches(text) }
        if (!solution && !wrong) return

        val chestPos = findWeirdoChest(npcName) ?: return
        if (solution) correctWeirdoChest = chestPos else wrongWeirdoChests += chestPos
    }

    private fun findWeirdoChest(npcName: String): BlockPos? {
        val level = world ?: return null
        val stand = level.entitiesForRendering().filterIsInstance<ArmorStand>().firstOrNull {
            it.name.stripped.contains(npcName)
        } ?: return null
        val base = BlockPos(floor(stand.x).toInt(), 69, floor(stand.z).toInt())

        for (dx in -2..2) for (dz in -2..2) {
            val pos = base.offset(dx, 0, dz)
            val block = level.getBlockState(pos).block
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) return pos
        }

        return null
    }

    private fun updateBlazes() {
        val level = world ?: return
        blazes.clear()
        blazeHealth.clear()

        level.entitiesForRendering().filterIsInstance<ArmorStand>().forEach { stand ->
            val name = stand.customName?.stripped ?: return@forEach
            val health = blazeHpRegex.find(name)?.groupValues?.getOrNull(1)?.replace(",", "")?.toIntOrNull() ?: return@forEach
            val blazeEntity = level.getEntitiesOfClass(Blaze::class.java, stand.boundingBox.inflate(1.0, 3.0, 1.0)).firstOrNull() ?: return@forEach
            if (blazeEntity.id in blazeHealth) return@forEach
            blazeHealth[blazeEntity.id] = health
            blazes += blazeEntity
        }

        blazes.sortBy { blazeHealth[it.id] ?: Int.MAX_VALUE }
        if (Dungeon.currentRoom?.name?.contains("Lower Blaze", ignoreCase = true) == true) blazes.reverse()
    }

    private fun renderBlazes() {
        blazes.take(3).forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                else -> blazeOtherColor
            }
            Render3D.drawEntityBox(entity, color, depth = false, lineWidth = 2f, expand = 0.15)
        }
    }

    private fun renderBlock(pos: BlockPos, color: Color) {
        Render3D.outlineBlock(pos, color, lineWidth = 2f, depth = false)
        Render3D.fillBlock(pos, color, depth = false)
    }

    private fun resetRoomState() {
        blazes.clear()
        blazeHealth.clear()
        quizAnswerBlocks.clear()
        expectedQuizAnswers = emptyList()
        resetWeirdos()
    }

    private fun resetWeirdos() {
        correctWeirdoChest = null
        wrongWeirdoChests.clear()
    }

    private val quizSolutions = mapOf(
        "What is the status of The Watcher?" to listOf("Stalker"),
        "What is the status of Bonzo?" to listOf("New Necromancer"),
        "What is the status of Scarf?" to listOf("Apprentice Necromancer"),
        "What is the status of Professor?" to listOf("Professor"),
        "What is the status of Thorn?" to listOf("Shaman Necromancer"),
        "What is the status of Livid?" to listOf("Master Necromancer"),
        "What is the name of the catacombs boss in Floor 1?" to listOf("Bonzo"),
        "What is the name of the catacombs boss in Floor 2?" to listOf("Scarf"),
        "What is the name of the catacombs boss in Floor 3?" to listOf("The Professor"),
        "What is the name of the catacombs boss in Floor 4?" to listOf("Thorn"),
        "What is the name of the catacombs boss in Floor 5?" to listOf("Livid"),
        "What is the name of the catacombs boss in Floor 6?" to listOf("Sadan"),
        "What is the name of the catacombs boss in Floor 7?" to listOf("Necron"),
        "What is the name of the mayor that increases dungeon score?" to listOf("Paul"),
        "Which brother is on the Spider's Den?" to listOf("Rick"),
        "Which villager in the Village gives you a Rogue Sword?" to listOf("Jamie")
    )

    private val weirdoSolutions = listOf(
        Regex("The reward is not in my chest!"),
        Regex("At least one of them is lying, and the reward is not in .+'s chest.?"),
        Regex("My chest doesn't have the reward. We are all telling the truth.?"),
        Regex("My chest has the reward and I'm telling the truth!"),
        Regex("The reward isn't in any of our chests.?"),
        Regex("Both of them are telling the truth. Also, .+ has the reward in their chest.?")
    )

    private val weirdoWrong = listOf(
        Regex("One of us is telling the truth!"),
        Regex("They are both telling the truth. The reward isn't in .+'s chest."),
        Regex("We are all telling the truth!"),
        Regex(".+ is telling the truth and the reward is in his chest."),
        Regex("My chest doesn't have the reward. At least one of the others is telling the truth!"),
        Regex("One of the others is lying."),
        Regex("They are both telling the truth, the reward is in .+'s chest."),
        Regex("They are both lying, the reward is in my chest!"),
        Regex("The reward is in my chest."),
        Regex("The reward is not in my chest. They are both lying."),
        Regex(".+ is telling the truth."),
        Regex("My chest has the reward.")
    )
}
