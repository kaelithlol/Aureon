package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.AureonCore.SHORTPREFIX
import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.events.core.ChatEvent
import com.kaelith.aureon.features.Feature
import com.kaelith.aureon.utils.config
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object TermTracker : Feature("termTracker", island = SkyBlockIsland.THE_CATACOMBS) {
    private val recap by config.property<Boolean>("termTracker.recap")

    private lateinit var completed: MutableMap<String, MutableMap<String, Int>>
    private val firstCompletion = mutableMapOf<String, Long>()
    private val lastCompletion = mutableMapOf<String, Long>()
    private val pattern = Regex("""^(\w{1,16}) (?:activated|completed) a (\w+)! \(\d/\d\)$""")
    private var phaseStartMs: Long? = null
    private var recapSent = false

    override fun initialize() {
        completed = mutableMapOf()
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            when {
                msg == "The Core entrance is opening!" -> {
                    completed.forEach { (user, data) ->
                        Signal.fakeMessage("$SHORTPREFIX §b$user §7completed §f${data["terminal"] ?: 0} §7 terms, §f${data["device"] ?: 0} §7devices, and §f${data["lever"] ?: 0} §7levers!")
                    }
                    if (recap) sendRecap()
                }
                pattern.matches(msg) -> {
                    val match = pattern.matchEntire(msg)!!
                    val (user, type) = match.destructured
                    if (type in listOf("terminal", "lever", "device")) {
                        val now = System.currentTimeMillis()
                        if (phaseStartMs == null) phaseStartMs = now
                        firstCompletion.putIfAbsent(user, now)
                        lastCompletion[user] = now
                        completed.getOrPut(user) { mutableMapOf() }[type] =
                            (completed[user]?.get(type) ?: 0) + 1
                    }
                }
            }
        }
    }

    private fun sendRecap() {
        if (recapSent) return
        recapSent = true

        val start = phaseStartMs ?: return
        val end = lastCompletion.values.maxOrNull() ?: start
        val slowest = lastCompletion.maxByOrNull { it.value }?.key ?: "Unknown"
        Signal.fakeMessage("$SHORTPREFIX §dTerminal Phase Recap §8| §7Time: §b${format(end - start)} §8| §7Players: §b${completed.size} §8| §7Last: §e$slowest")
    }

    private fun format(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    override fun onRegister() {
        if (this::completed.isInitialized) completed.clear()
        resetRecap()
        super.onRegister()
    }

    override fun onUnregister() {
        if (this::completed.isInitialized) completed.clear()
        resetRecap()
        super.onUnregister()
    }

    private fun resetRecap() {
        firstCompletion.clear()
        lastCompletion.clear()
        phaseStartMs = null
        recapSent = false
    }
}
