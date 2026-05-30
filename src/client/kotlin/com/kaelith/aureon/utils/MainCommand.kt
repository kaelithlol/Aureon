package com.kaelith.aureon.utils

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Command
import com.kaelith.aureon.api.dungeons.Dungeon
import com.kaelith.aureon.api.dungeons.score.DungeonScore
import com.kaelith.aureon.api.handlers.Atlas
import com.kaelith.aureon.api.handlers.AutoUpdater
import com.kaelith.aureon.api.handlers.Chronos
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.api.zenith.player
import com.kaelith.aureon.features.dungeons.DungeonSplits
import com.kaelith.aureon.features.dungeons.JoinInfo
import com.kaelith.aureon.features.msc.AutoFriend
import com.kaelith.aureon.features.msc.buttonUtils.ButtonLayoutEditor
import com.kaelith.aureon.features.secrets.utils.routes.RouteRecorder
import com.kaelith.aureon.hud.HUDEditor
@Command
object MainCommand : Atlas("aureon", "Aureon") {
    init {
        literal("hud") {
            runs {
                Chronos.Tick post {
                    client.setScreen(HUDEditor())
                }
            }
        }

        literal("buttons") {
            runs {
                Chronos.Tick post {
                    client.setScreen(ButtonLayoutEditor())
                }
            }
        }

        literal("route") {
            literal("start") {
                runs {
                    RouteRecorder.startRecording()
                }
            }

            literal("stop") {
                runs {
                    RouteRecorder.stopRecording()
                }
            }

            literal("save") {
                runs {
                    RouteRecorder.saveRoute()
                }
            }

            literal("reload") {
                runs {
                    RouteRecorder.reloadRoutes()
                }
            }

            literal("custom") {
                runs {
                    RouteRecorder.addCustom()
                }
            }

            literal("missing") {
                runs {
                    val missing = RouteRecorder.getMissing()
                    Signal.fakeMessage("${AureonCore.PREFIX} §bMissing Rooms §6${missing.size}§b:")
                    missing.forEach { Signal.fakeMessage("§7 - $it") }
                }
            }
        }

        literal("help") {
            runs {
                Signal.fakeMessage("§8§m------------------------------------------")
                Signal.fakeMessage("§6/aureon §7main command!")
                Signal.fakeMessage("§6/aureon help §7Opens the Aureon help menu!")
                Signal.fakeMessage("§6/aureon hud §7Opens the HUD editor!")
                Signal.fakeMessage("§6/aureon route §7Manage secret route recording.")
                Signal.fakeMessage("§6/aureon updates status §7Shows auto updater status.")
                Signal.fakeMessage("§6/aureon updates on §7Enables auto updates and checks now.")
                Signal.fakeMessage("§6/aureon summary copy §7Copies the last run recap.")
                Signal.fakeMessage("§8§m------------------------------------------")
            }
        }

        literal("dumpscore") {
            runs {
                if (Dungeon.floor == null) {
                    Signal.fakeMessage("${AureonCore.PREFIX} §cError: Not in dungeon")
                }

                val data = DungeonScore.data

                Signal.fakeMessage("§d§m------------------------------------------")
                Signal.fakeMessage("§bDungeon Score Breakdown §7(§6${Dungeon.floor?.name ?: "?"}§7)")
                Signal.fakeMessage("§d§m------------------------------------------")
                Signal.fakeMessage("                 §bScore: §6${data.score}")
                Signal.fakeMessage("")
                Signal.fakeMessage("§7Skill Score§8: §b${data.skillScore}")
                Signal.fakeMessage("§7Explore Score§8: §b${data.exploreScore}")
                Signal.fakeMessage("§7Speed Score§8: §b${data.speedScore}")
                Signal.fakeMessage("§7Bonus Score§8: §b${data.bonusScore}")
                Signal.fakeMessage("")
                Signal.fakeMessage("§d§m------------------------------------------")
            }
        }

        literal("summary") {
            literal("copy") {
                runs {
                    DungeonSplits.copyLastSummary()
                }
            }
        }

        literal("ready") {
            runs {
                DungeonSplits.runReadyCheck()
            }
        }

        literal("autofriend") {
            runs {
                val enabled = AutoFriend.toggle()
                Signal.fakeMessage("${AureonCore.PREFIX} \u00a7bAuto Friend: ${if (enabled) "\u00a7aON" else "\u00a7cOFF"}")
            }
        }

        literal("updates") {
            literal("status") {
                runs {
                    Signal.fakeMessage("${AureonCore.PREFIX} §b${AutoUpdater.statusLine}")
                }
            }

            literal("on") {
                runs {
                    AutoUpdater.setEnabledState(true)
                    AutoUpdater.checkForUpdatesAsync(manual = true)
                    Signal.fakeMessage("${AureonCore.PREFIX} §b${AutoUpdater.statusLine}")
                }
            }

        }

        literal("cata") {
            runs<Greedy?> ("name") { arg ->
                val name = arg?.string ?: player?.name?.string ?: return@runs
                JoinInfo.fetchAndDisplayStats(name)
            }
        }

        runs {
            config.open()
        }
    }
}
