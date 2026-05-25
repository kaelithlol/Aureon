package com.kaelith.aureon.api.handlers

import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.EventBus
import com.kaelith.aureon.events.core.ServerEvent

@Module
object Pulsar {
    var FirstInstall by Capsule("firstInstall", true)

    init {
        EventBus.on<ServerEvent.Connect> {
            if (FirstInstall) {
                Chronos.Tick.after(20 * 1) run {
                    Signal.fakeMessage(
                        "§b§l---------------------------------------------\n" +
                            "   §r§6Thank you for installing §d§lAureon§r§3!\n" +
                            "\n" +
                            "   §r§3Commands\n" +
                            "   §r§d/aureon help §3§l- §r§bFor a list of commands!\n" +
                            "\n" +
                            "   §r§dGithub:  https://github.com/kaelithlol/aureon\n" +
                            "   §r§dModrinth: https://modrinth.com/mod/aureon\n" +
                            "§b§l---------------------------------------------"
                    )

                    FirstInstall = false
                }
            }
        }
    }
}
