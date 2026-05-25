package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.score.DungeonScore
import com.kaelith.aureon.api.dungeons.score.MimicTrigger
import com.kaelith.aureon.api.dungeons.utils.DungeonClass
import com.kaelith.aureon.api.handlers.Capsule
import com.kaelith.aureon.api.handlers.Chronos
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.events.core.DungeonEvent
import com.kaelith.aureon.events.core.GuiEvent
import com.kaelith.aureon.events.core.LocationEvent
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.hud.HUDManager
import com.kaelith.aureon.utils.config
import com.kaelith.aureon.utils.render.Render2D
import com.google.gson.reflect.TypeToken
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object DungeonSplits : Feature("dungeonSplits", island = SkyBlockIsland.THE_CATACOMBS) {
    private const val HUD_ID = "dungeonSplitsHud"

    private val showHud by config.property<Boolean>("dungeonSplits.hud")
    private val showRecap by config.property<Boolean>("dungeonSplits.recap")
    private val showScoreSplits by config.property<Boolean>("dungeonSplits.scoreSplits")
    private val showPb by config.property<Boolean>("dungeonSplits.pb")
    private val showBloodCamp by config.property<Boolean>("dungeonSplits.bloodCamp")
    private val bloodCampWarnSeconds by config.property<Int>("dungeonSplits.bloodCampWarn")
    private val keyAlerts by config.property<Boolean>("dungeonSplits.keyAlerts")
    private val statusHud by config.property<Boolean>("dungeonSplits.statusHud")
    private val readyCheck by config.property<Boolean>("dungeonSplits.readyCheck")

    private var floor: DungeonFloor? = null
    private var started = false
    private var recapSent = false
    private var bloodCampWarned = false
    private var lastBossState = false

    private var startMs = 0L
    private var bloodOpenMs: Long? = null
    private var bloodClearMs: Long? = null
    private var bossEntryMs: Long? = null
    private var score270Ms: Long? = null
    private var score300Ms: Long? = null
    private var endMs: Long? = null
    private val deathRecords = mutableListOf<DeathRecord>()
    private val pickedKeys = mutableSetOf<String>()

    private val pbStore = Capsule(
        fileName = "dungeon_split_pbs",
        defaultObject = mutableMapOf<String, SplitPb>(),
        typeToken = object : TypeToken<MutableMap<String, SplitPb>>() {}
    )

    override fun initialize() {
        HUDManager.registerCustom(HUD_ID, 144, 106, { renderPreview(it) }, "dungeonSplits.hud")

        on<DungeonEvent.Start> { event ->
            resetRun()
            floor = event.floor
            started = true
            startMs = now()
            if (readyCheck) Chronos.Tick after 40 run { sendReadyCheck() }
        }

        on<DungeonEvent.Score.On270> { markScore270() }
        on<DungeonEvent.Score.On300> { markScore300() }

        on<TickEvent.Client> {
            if (!started) return@on
            if (bloodOpenMs == null && Dungeon.bloodClear) bloodOpenMs = elapsed()
            if (bloodClearMs == null && Dungeon.bloodDone) bloodClearMs = elapsed()
            if (!lastBossState && Dungeon.inBoss) bossEntryMs = elapsed()
            checkBloodCamp()
            lastBossState = Dungeon.inBoss
        }

        on<DungeonEvent.KeyPickUp> { event ->
            val keyName = event.key.name.lowercase().replaceFirstChar { it.titlecase() }
            pickedKeys += keyName
            if (keyAlerts) Signal.fakeMessage("${AureonCore.PREFIX} §b$keyName Key picked up")
        }

        on<DungeonEvent.Player.Death> { event ->
            if (!started) return@on
            deathRecords += DeathRecord(event.player.name, if (bossEntryMs == null) "During Clear" else "Boss")
        }

        on<DungeonEvent.End> { event ->
            floor = event.floor
            endMs = elapsed()
            updatePbs()
            if (showRecap) sendRecap()
        }

        on<GuiEvent.RenderHUD> { renderHud(it.context) }

        on<LocationEvent.ServerChange> { resetRun() }
        on<LocationEvent.IslandChange> { resetRun() }
    }

    private fun renderHud(context: GuiGraphicsExtractor) {
        if (!showHud) return
        HUDManager.renderHud(HUD_ID, context) { drawLines(context, hudLines()) }
    }

    private fun renderPreview(context: GuiGraphicsExtractor) {
        drawLines(context, previewLines)
    }

    private fun drawLines(context: GuiGraphicsExtractor, lines: List<String>) {
        lines.forEachIndexed { index, line ->
            Render2D.drawString(context, line, 0, index * 10, shadow = false)
        }
    }

    private fun hudLines(): List<String> {
        if (!started) return previewLines

        val score = DungeonScore.data.score
        val current = elapsed()
        return buildList {
            add("§dDungeon Splits §7${floor?.name ?: ""}")
            add("§7Run: §b${format(endMs ?: current)} §8| §7Score: §${scoreColor(score)}$score")
            add(splitLine("Blood", bloodOpenMs))
            add(splitLine("Watcher", bloodClearMs))
            add(splitLine("Boss", bossEntryMs))
            if (showBloodCamp && bloodOpenMs != null && bloodClearMs == null) {
                add("§7Blood Camp: §${bloodCampColor()}${format(current - bloodOpenMs!!)}")
            }
            if (showScoreSplits) {
                add(splitLine("270", score270Ms))
                add(splitLine("300", score300Ms))
            }
            if (showPb) add("§7PB: §b${pbLine(current)}")
            if (statusHud) add(statusLine())
        }
    }

    private fun sendRecap() {
        if (recapSent) return
        recapSent = true

        val data = DungeonScore.data
        lastSummary = buildSummaryLines(data)
        lastSummary.forEach { Signal.fakeMessage(it) }
        sendCopyPrompt()
    }

    private fun sendCopyPrompt() {
        val copyText = lastSummary.joinToString("\n") { it.stripCodes() }
        Signal.fakeMessage(
            Component.literal("§d| §7Click ")
                .append(
                    Component.literal("§a§l[COPY SUMMARY]")
                        .withStyle {
                            it.withClickEvent(ClickEvent.CopyToClipboard(copyText))
                                .withHoverEvent(HoverEvent.ShowText(Component.literal("Copy this run recap to clipboard")))
                        }
                )
                .append(" §7to copy this run recap.")
        )
    }

    private fun buildSummaryLines(data: com.kaelith.aureon.api.dungeons.score.ScoreData): List<String> = buildList {
        add("${AureonCore.PREFIX} §dRun Recap §7${floor?.name ?: ""}")
        add("§d| §7Time: §b${format(endMs ?: elapsed())} §8| §7Score: §${scoreColor(data.score)}${data.score} §8| §7Secrets: §b${data.secretsFound}§7/§b${data.totalSecrets}")
        add("§d| §7Crypts: §b${data.crypts} §8| §7Deaths: §${if (data.teamDeaths > 0) "c" else "a"}${data.teamDeaths} §8| §7Rooms: §b${data.completedRooms}§7/§b${data.totalRooms}")
        add("§d| §7Blood: §b${formatOrDash(bloodOpenMs)} §8| §7Watcher: §b${formatOrDash(bloodClearMs)} §8| §7Boss: §b${formatOrDash(bossEntryMs)}")
        if (showScoreSplits) add("§d| §7270: §b${formatOrDash(score270Ms)} §8| §7300: §b${formatOrDash(score300Ms)}")
        add("§d| §7Keys: §b${pickedKeys.ifEmpty { setOf("None") }.joinToString(", ")} §8| §7Mimic: ${check(MimicTrigger.mimicDead)} §8| §7Prince: ${check(MimicTrigger.princeDead)}")
        if (deathRecords.isNotEmpty()) {
            add("§d| §7Deaths: §c${deathRecords.joinToString(", ") { "${it.name} (${it.phase})" }}")
        }
    }

    private fun checkBloodCamp() {
        if (!showBloodCamp || bloodCampWarned) return
        val openedAt = bloodOpenMs ?: return
        if (bloodClearMs != null) return
        val campMs = elapsed() - openedAt
        if (campMs >= bloodCampWarnSeconds * 1000L) {
            bloodCampWarned = true
            Signal.fakeMessage("${AureonCore.PREFIX} §cBlood camp is at ${format(campMs)}")
        }
    }

    private fun updatePbs() {
        val floorName = floor?.name ?: return
        val run = SplitPb(endMs, bloodOpenMs, bloodClearMs, bossEntryMs, score270Ms, score300Ms)
        pbStore.update {
            val old = this[floorName]
            this[floorName] = old?.bestWith(run) ?: run
        }
    }

    private fun pbLine(current: Long): String {
        val floorName = floor?.name ?: return "--"
        val pb = pbStore.getData()[floorName]?.total ?: return "No PB"
        return delta(current - pb)
    }

    private fun statusLine(): String {
        val keys = pickedKeys.ifEmpty { setOf("No Keys") }.joinToString("/")
        return "§7Keys: §b$keys §8| §7M: ${check(MimicTrigger.mimicDead)} §8| §7P: ${check(MimicTrigger.princeDead)}"
    }

    private fun sendReadyCheck() {
        val players = Dungeon.players
        if (players.isEmpty()) {
            Signal.fakeMessage("${AureonCore.PREFIX} §cReady Check: no dungeon players found yet")
            return
        }

        val classCounts = players.groupingBy { it.dclass }.eachCount()
        val duplicates = classCounts
            .filterKeys { it !in setOf(DungeonClass.UNKNOWN, DungeonClass.DEAD) }
            .filterValues { it > 1 }
            .keys
            .joinToString(", ") { it.displayName }
            .ifBlank { "None" }
        val unknown = players.count { it.dclass == DungeonClass.UNKNOWN }

        Signal.fakeMessage("${AureonCore.PREFIX} §dDungeon Ready Check")
        Signal.fakeMessage("§d| §7Players: §${if (players.size == 5) "a" else "c"}${players.size}/5 §8| §7Duplicate Classes: §${if (duplicates == "None") "a" else "e"}$duplicates")
        if (unknown > 0) Signal.fakeMessage("§d| §7Unknown Classes: §e$unknown")
        Signal.fakeMessage("§d| §7Team: §b${players.joinToString(", ") { "${it.name} (${it.dclass.displayName})" }}")
    }

    fun copyLastSummary() {
        if (lastSummary.isEmpty()) {
            Signal.fakeMessage("${AureonCore.PREFIX} §cNo run summary to copy yet")
            return
        }
        client.keyboardHandler.clipboard = lastSummary.joinToString("\n") { it.stripCodes() }
        Signal.fakeMessage("${AureonCore.PREFIX} §aCopied last run summary")
    }

    fun runReadyCheck() = sendReadyCheck()

    private fun markScore270() {
        if (started && score270Ms == null) score270Ms = elapsed()
    }

    private fun markScore300() {
        if (started && score300Ms == null) score300Ms = elapsed()
    }

    private fun splitLine(label: String, value: Long?): String {
        val pb = pbFor(label)
        val extra = if (showPb && value != null && pb != null) " §8(${delta(value - pb)})" else ""
        return "§7$label: §b${formatOrDash(value)}$extra"
    }

    private fun formatOrDash(value: Long?): String = value?.let { format(it) } ?: "--:--"
    private fun bloodCampColor(): String = if ((elapsed() - (bloodOpenMs ?: elapsed())) >= bloodCampWarnSeconds * 1000L) "c" else "e"
    private fun check(value: Boolean): String = if (value) "§a✔" else "§c✘"
    private fun delta(ms: Long): String = "${if (ms <= 0) "§a-" else "§c+"}${format(kotlin.math.abs(ms))}"
    private fun String.stripCodes(): String = replace(Regex("§."), "")

    private fun pbFor(label: String): Long? {
        val floorName = floor?.name ?: return null
        val pb = pbStore.getData()[floorName] ?: return null
        return when (label) {
            "Blood" -> pb.blood
            "Watcher" -> pb.watcher
            "Boss" -> pb.boss
            "270" -> pb.score270
            "300" -> pb.score300
            else -> null
        }
    }

    private fun format(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private fun scoreColor(score: Int): String = when {
        score >= 300 -> "a"
        score >= 270 -> "e"
        else -> "c"
    }

    private fun elapsed(): Long = now() - startMs
    private fun now(): Long = System.currentTimeMillis()

    private fun resetRun() {
        floor = null
        started = false
        recapSent = false
        bloodCampWarned = false
        lastBossState = false
        startMs = 0L
        bloodOpenMs = null
        bloodClearMs = null
        bossEntryMs = null
        score270Ms = null
        score300Ms = null
        endMs = null
        deathRecords.clear()
        pickedKeys.clear()
    }

    private val previewLines = listOf(
        "§dDungeon Splits §7F7",
        "§7Run: §b6:42 §8| §7Score: §a300",
        "§7Blood: §b1:18",
        "§7Watcher: §b2:06",
        "§7Boss: §b4:11",
        "§7270: §b3:42",
        "§7300: §b5:58"
    )

    data class SplitPb(
        val total: Long? = null,
        val blood: Long? = null,
        val watcher: Long? = null,
        val boss: Long? = null,
        val score270: Long? = null,
        val score300: Long? = null
    ) {
        fun bestWith(other: SplitPb) = SplitPb(
            total = best(total, other.total),
            blood = best(blood, other.blood),
            watcher = best(watcher, other.watcher),
            boss = best(boss, other.boss),
            score270 = best(score270, other.score270),
            score300 = best(score300, other.score300)
        )

        private fun best(a: Long?, b: Long?): Long? = when {
            a == null -> b
            b == null -> a
            else -> minOf(a, b)
        }
    }

    private data class DeathRecord(val name: String, val phase: String)

    private var lastSummary: List<String> = emptyList()
}
