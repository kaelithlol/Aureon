package com.kaelith.aureon.utils

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.api.config.core.Config
import com.kaelith.aureon.api.zenith.Zenith
import com.kaelith.aureon.api.zenith.client
import com.kaelith.aureon.features.msc.buttonUtils.ButtonLayoutEditor
import com.kaelith.aureon.features.secrets.utils.routes.RouteRecorder
import com.kaelith.aureon.features.secrets.utils.routes.RouteRegistry
import com.kaelith.aureon.hud.HUDEditor
import net.minecraft.util.Util
import java.awt.Color
import java.net.URI

val config = Config(AureonCore.NAMESPACE) {
    category("General") {
        subcategory("Info") {
            textparagraph {
                configName = "info"
                name = "Aureon"
                description = "§bDungeon & QOL Mod" +
                        "\n§bMade by §dkaelith" +
                        "\n§bCommand: §6/aureon"
            }

        }

        subcategory("Socials") {
            button {
                configName = "website"
                name = "Modrinth"
                description = "."

                onclick {
                    val uri = URI("https://modrinth.com/mod/aureon")
                    Util.getPlatform().openUri(uri)
                }
            }

            button {
                configName = "source"
                name = "Github"
                description = "Aureon's github page"

                onclick {
                    val uri = URI("https://github.com/kaelithlol/aureon")
                    Util.getPlatform().openUri(uri)
                }
            }
        }

        subcategory("Shortcuts") {
            toggle {
                configName = "cosmetics"
                name = "Cosmetics"
                description = "Shows cosmetics"
                default = true
            }

            button {
                configName = "hudEditor"
                name = "Hud Editor"
                description = "Opens Aureon's Hud Editor (/aureon hud)"

                onclick {
                    client.setScreen(HUDEditor())
                }
            }
        }
    }

    category("Dungeons") {
        subcategory("Room Name", "showRoomName", "Shows the current map rooms name in a hud") {
            toggle {
                configName = "roomNameChroma"
                name = "Chroma"
                description = "Makes the room name chroma (Requires SBA or Skyhanni)"
            }
        }

        subcategory("Terminals") {
            toggle {
                configName = "termNumbers"
                name = "Enabled"
                description = "Shows terminal numbers and class labels in F7/M7 boss."
                default = false
            }

            dropdown {
                configName = "selectedRole"
                name = "Your Role"
                description = "Which class you are playing for terminal assignments."
                options = listOf("Tank", "Mage", "Bers", "Arch", "Heal", "All")
                default = 4 // All
                shouldShow { it["termNumbers"] as Boolean }
            }

            dropdown {
                configName = "preset"
                name = "Role Presets"
                description = "Which roll presets you want to use (from M7 Guides)"
                options = listOf("F7", "SL M7", "Low M7", "Mid M7", "High M7")
                default = 1 // All
                shouldShow { it["termNumbers"] as Boolean }
            }

            toggle {
                configName = "showTermClass"
                name = "Show Class Label"
                description = "Displays the class label next to each terminal."
                default = false
                shouldShow { it["termNumbers"] as Boolean }
            }

            toggle {
                configName = "hideNumber"
                name = "Hide Terminal Number"
                description = "Hides the terminal number and only shows the class label."
                default = false
                shouldShow { settings ->
                    (settings["termNumbers"] as Boolean) &&
                            (settings["showTermClass"] as Boolean)
                }
            }

            toggle {
                configName = "highlightTerms"
                name = "Highlight Term"
                description = "Outlines terminals in the world."
                default = false
                shouldShow { it["termNumbers"] as Boolean }
            }

            colorpicker {
                configName = "termColor"
                name = "Highlight Color"
                description = "Color used when not using class colors."
                default = Color(0, 255, 255, 255)
                shouldShow { settings ->
                    (settings["termNumbers"] as Boolean) &&
                            (settings["highlightTerms"] as Boolean) &&
                            !(settings["classColor"] as Boolean)
                }
            }

            toggle {
                configName = "classColor"
                name = "Use Class Color"
                description = "Colors terminals based on the assigned class."
                default = false
                shouldShow { it["termNumbers"] as Boolean && it["showTermClass"] as Boolean && it["highlightTerms"] as Boolean }
            }

            toggle {
                configName = "termTracker"
                name = "Terminal Tracker"
                description = "Tracks terminals, devices, and levers during boss."
                default = false
            }

            toggle {
                configName = "termTracker.recap"
                name = "Terminal Recap"
                description = "Shows total terminal phase time and last finisher"
                default = true
                shouldShow { it["termTracker"] as Boolean }
            }
        }

        subcategory("Leap Announce", "leapAnnounce", "Announces when you leap to someone") {
            textinput {
                configName = "leapAnnounce.message"
                name = "Message"
                description = $$"The message to send on leap ($player will be replaced with player name)"
                placeholder = "Leaping to"
            }
        }

        subcategory("Teammate Missing Alert", "teammateMissing", "Alerts you when less than 5 people are in your dungeon")

        /*
        subcategory("Block Overlay") {
            toggle {
                configName = "enableDungBlockOverlay"
                name = "Enable Block Overlay"
                description = "Replaces map block textures with colored overlays"
                default = true
            }

            toggle {
                configName = "dungeonBlocksEverywhere"
                name = "Render Outside Dungeons"
                description = "Shows block overlays even outside of dungeons"
                default = false
                shouldShow { settings -> settings["enableDungBlockOverlay"] as Boolean }
            }

            colorpicker {
                configName = "dungCrackedColour"
                name = "Cracked Brick Color"
                description = "Color used for cracked stone bricks"
                default = Color(255, 0, 255, 255)
            }

            colorpicker {
                configName = "dungDispenserColour"
                name = "Dispenser Color"
                description = "Color used for dispensers"
                default = Color(255, 255, 0, 255)
            }

            colorpicker {
                configName = "dungLeverColour"
                name = "Lever Color"
                description = "Color used for levers"
                default = Color(0, 255, 0, 255)
            }

            colorpicker {
                configName = "dungTripWireColour"
                name = "Tripwire Color"
                description = "Color used for tripwires"
                default = Color(0, 255, 255, 255)
            }

            colorpicker {
                configName = "dungBatColour"
                name = "Bat Color"
                description = "Color used for map bats"
                default = Color(255, 100, 255, 255)
            }

            colorpicker {
                configName = "dungChestColour"
                name = "Chest Color"
                description = "Color used for normal map chests"
                default = Color(255, 150, 0, 255)
            }

            colorpicker {
                configName = "dungTrappedChestColour"
                name = "Trapped Chest Color"
                description = "Color used for trapped map chests"
                default = Color(255, 0, 0, 255)
            }
        }
         */

        subcategory("Class Colors") {
            colorpicker {
                configName = "healerColor"
                name = "Healer Color"
                description = "Color used for Healer class"
                default = Color(240, 70, 240, 255)
            }

            colorpicker {
                configName = "mageColor"
                name = "Mage Color"
                description = "Color used for Mage class"
                default = Color(70, 210, 210, 255)
            }

            colorpicker {
                configName = "berzColor"
                name = "Berserker Color"
                description = "Color used for Berserker class"
                default = Color(255, 0, 0, 255)
            }

            colorpicker {
                configName = "archerColor"
                name = "Archer Color"
                description = "Color used for Archer class"
                default = Color(254, 223, 0, 255)
            }

            colorpicker {
                configName = "tankColor"
                name = "Tank Color"
                description = "Color used for Tank class"
                default = Color(30, 170, 50, 255)
            }
        }

        subcategory("Score Alerts", "scoreAlerts", "Enables alerts for dungeon score milestones") {
            toggle {
                configName = "forcePaul"
                name = "Force Paul"
                description = "Forces Paul's EZPZ +10 score"
                default = false
            }

            toggle {
                configName = "scoreAlerts.alert270"
                name = "270 Score Alert"
                description = "Alerts you when your score reaches 270"
                default = false
            }

            textinput {
                configName = "scoreAlerts.message270"
                name = "270 Score Message"
                description = "Message to display when reaching 270 score"
                placeholder = "&d270 score!"
                shouldShow { settings -> settings["scoreAlerts.alert270"] as Boolean }
            }

            toggle {
                configName = "scoreAlerts.chat270"
                name = "270 Chat Alert"
                description = "Sends a chat message when reaching 270 score"
                default = false
            }

            textinput {
                configName = "scoreAlerts.chatMessage270"
                name = "270 Chat Message"
                description = "Chat message to send when reaching 270 score"
                placeholder = "270 score!"
                shouldShow { settings -> settings["scoreAlerts.chat270"] as Boolean }
            }

            toggle {
                configName = "scoreAlerts.alert300"
                name = "300 Score Alert"
                description = "Alerts you when your score reaches 300"
                default = false
            }

            textinput {
                configName = "scoreAlerts.message300"
                name = "300 Score Message"
                description = "Message to display when reaching 300 score"
                placeholder = "&d300 score!"
                shouldShow { settings -> settings["scoreAlerts.alert300"] as Boolean }
            }

            toggle {
                configName = "scoreAlerts.chat300"
                name = "300 Chat Alert"
                description = "Sends a chat message when reaching 300 score"
                default = false
            }

            textinput {
                configName = "scoreAlerts.chatMessage300"
                name = "300 Chat Message"
                description = "Chat message to send when reaching 300 score"
                placeholder = "300 score!"
                shouldShow { settings -> settings["scoreAlerts.chat300"] as Boolean }
            }

            toggle {
                configName = "scoreAlerts.alert5Crypts"
                name = "5 Crypts Alert"
                description = "Alerts you when your team reaches 5 crypts"
                default = false
            }

            textinput {
                configName = "scoreAlerts.message5Crypts"
                name = "5 Crypts Message"
                description = "Message to display when reaching 5 crypts"
                placeholder = "&d5 crypts!"
                shouldShow { settings -> settings["scoreAlerts.alert5Crypts"] as Boolean }
            }

            toggle {
                configName = "scoreAlerts.chat5Crypts"
                name = "5 Crypts Chat Alert"
                description = "Sends a chat message when reaching 5 crypts"
                default = false
            }

            textinput {
                configName = "scoreAlerts.chatMessage5Crypts"
                name = "5 Crypts Chat Message"
                description = "Chat message to send when reaching 5 crypts"
                placeholder = "5 crypts!"
                shouldShow { settings -> settings["scoreAlerts.chat5Crypts"] as Boolean }
            }

            toggle {
                configName = "scoreAlerts.alertBatDead"
                name = "Bat Dead Alert"
                description = "Alerts you when a bat dies"
            }

            textinput {
                configName = "scoreAlerts.messageBatDead"
                name = "Bat Dead Message"
                description = "Message to display when a bat dies"
                placeholder = "&cBat Dead"
                shouldShow { settings -> settings["scoreAlerts.alertBatDead"] as Boolean }
            }

        }

        subcategory("Dungeon Splits", "dungeonSplits", "Shows live dungeon splits and a run recap") {
            toggle {
                configName = "dungeonSplits.hud"
                name = "Splits HUD"
                description = "Shows live split timers while in a dungeon"
                default = true
            }

            toggle {
                configName = "dungeonSplits.recap"
                name = "Run Recap"
                description = "Shows a local chat recap when the dungeon ends"
                default = true
            }

            toggle {
                configName = "dungeonSplits.scoreSplits"
                name = "Score Splits"
                description = "Includes 270 and 300 score split timers"
                default = true
            }

            toggle {
                configName = "dungeonSplits.pb"
                name = "PB Tracking"
                description = "Tracks best split times per floor"
                default = true
            }

            toggle {
                configName = "dungeonSplits.bloodCamp"
                name = "Blood Camp Timer"
                description = "Shows and warns about time spent camping blood"
                default = true
            }

            stepslider {
                configName = "dungeonSplits.bloodCampWarn"
                name = "Blood Camp Warning"
                description = "Seconds before warning about blood camp"
                min = 15
                max = 120
                step = 5
                default = 45
                shouldShow { it["dungeonSplits.bloodCamp"] as Boolean }
            }

            toggle {
                configName = "dungeonSplits.keyAlerts"
                name = "Key Alerts"
                description = "Shows a local alert when dungeon keys are picked up"
                default = true
            }

            toggle {
                configName = "dungeonSplits.statusHud"
                name = "Status HUD"
                description = "Adds keys, mimic, and prince status to the splits HUD"
                default = true
            }

            toggle {
                configName = "dungeonSplits.readyCheck"
                name = "Ready Check"
                description = "Shows player count and class duplicates near run start"
                default = true
            }
        }

        subcategory("Crypt Reminder", "cryptReminder", "reminds you to do your crypts") {
            stepslider {
                name = "Delay"
                configName = "cryptReminder.delay"
                description = "Message delay in minutes"
                min = 1
                max = 5
                default = 2
            }

            toggle {
                name = "Send to Party"
                configName = "cryptReminder.sendParty"
                description = "Send message to the party"
            }
        }

        subcategory("Dungeon QoL", "dungeonQoL", "Extra dungeon quality-of-life features") {
            toggle {
                configName = "dungeonQoL.autoRequeue"
                name = "Auto Requeue"
                description = "Runs /instancerequeue after a dungeon ends"
                default = false
            }

            toggle {
                configName = "dungeonQoL.extraStats"
                name = "Extra Stats"
                description = "Runs /showextrastats after a dungeon ends"
                default = false
            }

            toggle {
                configName = "dungeonQoL.mimicAnnounce"
                name = "Mimic Announce"
                description = "Sends a party message when Mimic dies"
                default = false
            }

            toggle {
                configName = "dungeonQoL.princeAnnounce"
                name = "Prince Announce"
                description = "Sends a party message when Prince dies"
                default = false
            }

            toggle {
                configName = "dungeonQoL.secretSounds"
                name = "Secret Sounds"
                description = "Plays a sound when secrets are clicked, picked up, or killed"
                default = false
            }
        }

        subcategory("Blood ESP", "bloodEsp", "Highlights the Blood room before the dungeon starts") {
            toggle {
                configName = "bloodEsp.roomBox"
                name = "Room Box"
                description = "Draws a box around the Blood room"
                default = true
            }

            toggle {
                configName = "bloodEsp.tracer"
                name = "Door Tracer"
                description = "Draws a tracer toward the Blood room"
                default = true
            }

            colorpicker {
                configName = "bloodEsp.color"
                name = "Color"
                description = "Blood room ESP color"
                default = Color(255, 0, 0, 150)
            }
        }

        subcategory("Door Key ESP", "doorKeysEsp", "Highlights dropped Wither and Blood keys") {
            toggle {
                configName = "doorKeysEsp.wither"
                name = "Wither Key"
                description = "Highlight Wither Key drops"
                default = true
            }

            toggle {
                configName = "doorKeysEsp.blood"
                name = "Blood Key"
                description = "Highlight Blood Key drops"
                default = true
            }

            colorpicker {
                configName = "doorKeysEsp.witherColor"
                name = "Wither Color"
                description = "ESP color for Wither Key drops"
                default = Color(0, 0, 0, 110)
                shouldShow { it["doorKeysEsp.wither"] as Boolean }
            }

            colorpicker {
                configName = "doorKeysEsp.bloodColor"
                name = "Blood Color"
                description = "ESP color for Blood Key drops"
                default = Color(255, 0, 0, 110)
                shouldShow { it["doorKeysEsp.blood"] as Boolean }
            }
        }

        subcategory("Boss Bar Health", "bossBarHealth", "Shows estimated boss health on dungeon boss bars") {
            toggle {
                configName = "bossBarHealth.watcher"
                name = "The Watcher"
                description = "Show health for The Watcher"
                default = true
            }

            toggle {
                configName = "bossBarHealth.thorn"
                name = "Thorn"
                description = "Show health for Thorn"
                default = true
            }

            toggle {
                configName = "bossBarHealth.withers"
                name = "F7 Withers"
                description = "Show health for Maxor, Storm, Goldor, and Necron"
                default = true
            }
        }

        subcategory("Architect Draft", "architectDraft", "Draft helpers for failed puzzle rooms") {
            toggle {
                configName = "architectDraft.announce"
                name = "Announce Draft"
                description = "Announces when you use Architect's First Draft"
                default = true
            }

            toggle {
                configName = "architectDraft.autoGet"
                name = "Auto Get Draft"
                description = "Runs /gfs after you fail a puzzle"
                default = false
            }
        }

        subcategory("Leap Menu", "leapMenu", "Adds quick leap keys and leap messages") {
            toggle {
                configName = "leapMenu.announce"
                name = "Announce Leap"
                description = "Sends a party message when you leap"
                default = false
            }

            textinput {
                configName = "leapMenu.message"
                name = "Leap Message"
                description = "{name} is replaced with the player you leaped to"
                placeholder = "Leaping to {name}"
                shouldShow { it["leapMenu.announce"] as Boolean }
            }

            toggle {
                configName = "leapMenu.hideNearby"
                name = "Hide Nearby"
                description = "Briefly hides nearby teammates after leaping"
                default = false
            }

            keybind {
                configName = "leapMenu.slot1"
                name = "Slot 1"
                description = "Clicks the first player head in the leap menu"
                default = Zenith.Keys.N_1
            }

            keybind {
                configName = "leapMenu.slot2"
                name = "Slot 2"
                description = "Clicks the second player head in the leap menu"
                default = Zenith.Keys.N_2
            }

            keybind {
                configName = "leapMenu.slot3"
                name = "Slot 3"
                description = "Clicks the third player head in the leap menu"
                default = Zenith.Keys.N_3
            }

            keybind {
                configName = "leapMenu.slot4"
                name = "Slot 4"
                description = "Clicks the fourth player head in the leap menu"
                default = Zenith.Keys.N_4
            }
        }

        subcategory("Auto GFS", "autoGfs", "Automatically refills dungeon items from sacks") {
            stepslider {
                configName = "autoGfs.delay"
                name = "Check Delay"
                description = "Seconds between refill checks"
                min = 5
                max = 60
                step = 5
                default = 20
            }

            toggle {
                configName = "autoGfs.pearls"
                name = "Ender Pearls"
                description = "Refill ender pearls up to 16"
                default = false
            }

            toggle {
                configName = "autoGfs.superboom"
                name = "Superboom TNT"
                description = "Refill Superboom TNT up to 64"
                default = false
            }

            toggle {
                configName = "autoGfs.jerry"
                name = "Inflatable Jerry"
                description = "Refill Inflatable Jerry up to 64"
                default = false
            }

            toggle {
                configName = "autoGfs.leaps"
                name = "Spirit Leaps"
                description = "Refill Spirit Leaps up to 16"
                default = false
            }

            toggle {
                configName = "autoGfs.twilight"
                name = "Twilight Poison"
                description = "Refill Twilight Arrow Poison during M7 triggers"
                default = false
            }
        }

        subcategory("Room Alerts", "roomAlerts", "Alerts for clear and secrets done in your current room") {
            toggle {
                configName = "roomAlerts.clear"
                name = "Cleared"
                description = "Shows a title when your room clears"
                default = true
            }

            toggle {
                configName = "roomAlerts.secrets"
                name = "Secrets Done"
                description = "Shows a title when your room turns green"
                default = true
            }
        }

        subcategory("F7/M7 Helpers", "floor7Helpers", "Extra helpers for F7 and M7 boss phases") {
            toggle {
                configName = "floor7Helpers.witherEsp"
                name = "Wither ESP"
                description = "Highlights active F7/M7 wither bosses"
                default = true
            }

            colorpicker {
                configName = "floor7Helpers.witherColor"
                name = "Wither Color"
                description = "Wither ESP color"
                default = Color(255, 255, 255, 180)
                shouldShow { it["floor7Helpers.witherEsp"] as Boolean }
            }

            toggle {
                configName = "floor7Helpers.crystalTimer"
                name = "Crystal Timer"
                description = "Shows Maxor crystal respawn timing"
                default = true
            }

            toggle {
                configName = "floor7Helpers.crystalPlace"
                name = "Crystal Place Time"
                description = "Shows your crystal place time in chat"
                default = true
            }

            toggle {
                configName = "floor7Helpers.relicTimer"
                name = "Relic Timer"
                description = "Shows M7 relic spawn timing"
                default = true
            }

            toggle {
                configName = "floor7Helpers.relicBoxes"
                name = "Relic Boxes"
                description = "Highlights the cauldron for the relic you are holding"
                default = true
            }
        }

        subcategory("Puzzle Solvers", "puzzleSolvers", "Dungeon puzzle solver highlights") {
            toggle {
                configName = "puzzleSolvers.quiz"
                name = "Quiz Solver"
                description = "Highlights the correct Quiz answer from chat"
                default = true
            }

            colorpicker {
                configName = "puzzleSolvers.quizColor"
                name = "Quiz Color"
                description = "Correct answer highlight color"
                default = Color(0, 255, 255, 120)
                shouldShow { it["puzzleSolvers.quiz"] as Boolean }
            }

            toggle {
                configName = "puzzleSolvers.threeWeirdos"
                name = "Three Weirdos"
                description = "Highlights the correct Three Weirdos chest"
                default = true
            }

            colorpicker {
                configName = "puzzleSolvers.weirdosCorrectColor"
                name = "Correct Chest"
                description = "Correct chest highlight color"
                default = Color(0, 255, 0, 120)
                shouldShow { it["puzzleSolvers.threeWeirdos"] as Boolean }
            }

            colorpicker {
                configName = "puzzleSolvers.weirdosWrongColor"
                name = "Wrong Chest"
                description = "Wrong chest highlight color"
                default = Color(255, 0, 0, 90)
                shouldShow { it["puzzleSolvers.threeWeirdos"] as Boolean }
            }

            toggle {
                configName = "puzzleSolvers.blaze"
                name = "Blaze Solver"
                description = "Highlights Blaze kill order"
                default = true
            }

            colorpicker {
                configName = "puzzleSolvers.blazeFirstColor"
                name = "First Blaze"
                description = "First Blaze highlight color"
                default = Color(0, 255, 0, 180)
                shouldShow { it["puzzleSolvers.blaze"] as Boolean }
            }

            colorpicker {
                configName = "puzzleSolvers.blazeSecondColor"
                name = "Second Blaze"
                description = "Second Blaze highlight color"
                default = Color(255, 255, 0, 180)
                shouldShow { it["puzzleSolvers.blaze"] as Boolean }
            }

            colorpicker {
                configName = "puzzleSolvers.blazeOtherColor"
                name = "Other Blazes"
                description = "Other Blaze highlight color"
                default = Color(255, 0, 0, 180)
                shouldShow { it["puzzleSolvers.blaze"] as Boolean }
            }
        }

        subcategory("Join Info", "joinInfo", "Shows extra info when someone joins your party")
    }

    category("AureonNav") {
        subcategory("Map", "mapEnabled", "Enables the dungeon map") {
            toggle {
                configName = "bossMapEnabled"
                name = "Enable Boss Map"
                description = "Enables the map boss map"
                default = false
            }

            toggle {
                configName = "hideInBoss"
                name = "Hide in Boss"
                description = "Hides the map in the boss"
                default = true
                shouldShow { settings -> !(settings["bossMapEnabled"] as Boolean) }
            }

            toggle {
                configName = "scoreMapEnabled"
                name = "Enable Score Map"
                description = "Enables the map score map"
                default = false
            }

            toggle {
                configName = "mapInfoUnder"
                name = "Info Under Map"
                description = "Renders map info below the map"
                default = false
            }
        }

        subcategory("Display") {
            colorpicker {
                configName = "mapBgColor"
                name = "Background Color"
                description = "Background color of the map"
                default = Color(0, 0, 0, 100)
            }

            toggle {
                configName = "mapBorder"
                name = "Map Border"
                description = "Renders a border around the map"
                default = true
            }

            colorpicker {
                configName = "mapBdColor"
                name = "Border Color"
                description = "Color of the map border"
                default = Color(0, 0, 0, 255)
                shouldShow { settings -> settings["mapBorder"] as Boolean }
            }

            stepslider {
                configName = "mapBdWidth"
                name = "Border Width"
                description = "The width of the map border"
                min = 1
                max = 5
                step = 1
                default = 2
                shouldShow { settings -> settings["mapBorder"] as Boolean }
            }

            slider {
                configName = "checkmarkScale"
                name = "Checkmark Size"
                description = "Size of the checkmarks"
                min = 0.1f
                max = 2f
                default = 1f
            }

            slider {
                configName = "nameScale"
                name = "Name Size"
                description = "Size of room name text"
                min = 0.1f
                max = 2f
                default = 1f
            }

            slider {
                configName = "secretScale"
                name = "Secret Size"
                description = "Size of room secret text"
                min = 0.1f
                max = 2f
                default = 1f
            }

            toggle {
                configName = "mtextshadow"
                name = "Text Shadow"
                description = "Gives the text a cool shadow"
                default = false
            }
        }

        subcategory("Behavior") {
            toggle {
                configName = "roomCheck"
                name = "Room Checkmarks"
                description = "Displays room checkmarks"
                default = true
            }

            toggle {
                configName = "roomName"
                name = "Room Name"
                description = "Displays room name"
            }

            toggle {
                configName = "roomSecrets"
                name = "Room Secrets"
                description = "Displays room secrets"
            }

            toggle {
                configName = "puzzleCheck"
                name = "Puzzle Checkmarks"
                description = "Displays puzzle checkmarks"
                default = true
            }

            toggle {
                configName = "puzzleName"
                name = "Puzzle Name"
                description = "Displays puzzle name"
            }

            toggle {
                configName = "puzzleSecrets"
                name = "Puzzle Secrets"
                description = "Displays puzzle secrets"
            }

            dropdown {
                configName = "checkAnchor"
                name = "Check Anchor"
                description = "Which component anchor to prioritize when rendering checkmarks"
                options = listOf("First", "Middle", "Last", "Center")
                default = 0 // First
            }

            dropdown {
                configName = "nameAnchor"
                name = "Name Anchor"
                description = "Which component anchor to prioritize when rendering room name"
                options = listOf("First", "Middle", "Last", "Center")
                default = 3 // Center
            }

            dropdown {
                configName = "secretsAnchor"
                name = "Secrets Anchor"
                description = "Which component anchor to prioritize when rendering room secrets"
                options = listOf("First", "Middle", "Last", "Center")
                default = 3 // Center
            }

            toggle {
                configName = "prioMiddle"
                name = "Prioritize middle"
                description = "Move center anchors to middle in 1x2 and L shaped rooms"
            }

            toggle {
                configName = "replaceText"
                name = "Replace Text"
                description = "Replace room text with checkmark on complete (overrides checkmark)"
            }
        }

        subcategory("Player Icons") {
            slider {
                configName = "iconScale"
                name = "Icon Scale"
                description = "Scale of the player icons"
                min = 0.1f
                max = 2f
                default = 1f
            }

            toggle {
                configName = "smoothMovement"
                name = "Smooth Movement"
                description = "Smooths marker movement"
                default = true
            }

            toggle {
                configName = "showPlayerHeads"
                name = "Player Heads"
                description = "Use player heads instead of map markers"
                default = false
            }

            toggle {
                configName = "ownDefault"
                name = "Default Self"
                description = "Use default marker for yourself"
                shouldShow { settings -> settings["showPlayerHeads"] as Boolean }
            }

            slider {
                configName = "iconBorderWidth"
                name = "Border Width"
                description = "The width of the icon border"
                min = 0f
                max = 1f
                default = 0.2f
            }

            colorpicker {
                configName = "iconBorderColor"
                name = "Border Color"
                description = "The color for the icon border"
                default = Color(0, 0, 0, 255)
            }

            toggle {
                configName = "iconClassColors"
                name = "Class Colors"
                description = "Use the color for the players class for the icon border"
                default = false
            }

            toggle {
                configName = "showNames"
                name = "Show Player Names"
                description = "Render player names under map icons"
            }

            toggle {
                configName = "dontShowOwn"
                name = "Hide Own Name"
                description = "Hides your name on the map"
                shouldShow { settings -> settings["showNames"] as Boolean }
            }

        }

        subcategory("Room Colors") {
            colorpicker {
                configName = "normalRoomColor"
                name = "Normal"
                default = Color(107, 58, 17, 255)
            }
            colorpicker {
                configName = "puzzleRoomColor"
                name = "Puzzle"
                default = Color(117, 0, 133, 255)
            }
            colorpicker {
                configName = "trapRoomColor"
                name = "Trap"
                default = Color(216, 127, 51, 255)
            }
            colorpicker {
                configName = "minibossRoomColor"
                name = "Miniboss"
                default = Color(254, 223, 0, 255)
            }
            colorpicker {
                configName = "bloodRoomColor"
                name = "Blood"
                default = Color(255, 0, 0, 255)
            }
            colorpicker {
                configName = "fairyRoomColor"
                name = "Fairy"
                default = Color(224, 0, 255, 255)
            }
            colorpicker {
                configName = "entranceRoomColor"
                name = "Entrance"
                default = Color(20, 133, 0, 255)
            }
        }

        subcategory("Door Colors") {
            colorpicker {
                configName = "normalDoorColor"
                name = "Normal Door"
                default = Color(80, 40, 10, 255)
            }
            colorpicker {
                configName = "witherDoorColor"
                name = "Wither Door"
                default = Color(0, 0, 0, 255)
            }
            colorpicker {
                configName = "bloodDoorColor"
                name = "Blood Door"
                default = Color(255, 0, 0, 255)
            }
            colorpicker {
                configName = "entranceDoorColor"
                name = "Entrance Door"
                default = Color(0, 204, 0, 255)
            }
        }

        subcategory("Extra") {
            toggle {
                configName = "boxWitherDoors"
                name = "Box Wither Doors"
                description = "Renders a box around wither doors"
                default = false
            }

            colorpicker {
                configName = "keyColor"
                name = "Key Color"
                description = "Color for doors with keys"
                default = Color(0, 255, 0, 255)
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            colorpicker {
                configName = "noKeyColor"
                name = "No Key Color"
                description = "Color for doors without keys"
                default = Color(255, 0, 0, 255)
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            colorpicker {
                configName = "keyFillColor"
                name = "Key Fill Color"
                description = "Fill color for wither/blood doors when a key is available"
                default = Color(0, 255, 0, 64)
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            colorpicker {
                configName = "noKeyFillColor"
                name = "No Key Fill Color"
                description = "Fill color for locked wither doors"
                default = Color(255, 0, 0, 64)
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            colorpicker {
                configName = "bloodBoxDoorColor"
                name = "Blood Door Locked Color"
                description = "Outline color for the blood door before blood key"
                default = Color(255, 0, 0, 255)
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            colorpicker {
                configName = "bloodDoorFillColor"
                name = "Blood Door Fill Color"
                description = "Fill color for the blood door before blood key"
                default = Color(255, 0, 0, 64)
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            toggle {
                configName = "renderNormalDoorBoxes"
                name = "Highlight Normal Doors"
                description = "Also highlights normal dungeon doors"
                default = false
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            toggle {
                configName = "hideGreenNormalDoorBoxes"
                name = "Hide Useless Normal Doors"
                description = "Skips normal doors that only lead to green-checked or entrance rooms"
                default = true
                shouldShow { settings -> (settings["boxWitherDoors"] as Boolean) && (settings["renderNormalDoorBoxes"] as Boolean) }
            }

            colorpicker {
                configName = "normalBoxDoorColor"
                name = "Normal Door Color"
                description = "Outline color for normal door boxes"
                default = Color(0, 128, 128, 255)
                shouldShow { settings -> (settings["boxWitherDoors"] as Boolean) && (settings["renderNormalDoorBoxes"] as Boolean) }
            }

            colorpicker {
                configName = "normalDoorFillColor"
                name = "Normal Door Fill Color"
                description = "Fill color for normal door boxes"
                default = Color(0, 128, 128, 32)
                shouldShow { settings -> (settings["boxWitherDoors"] as Boolean) && (settings["renderNormalDoorBoxes"] as Boolean) }
            }

            toggle {
                configName = "doorFillPhase"
                name = "Door Fill Through Walls"
                description = "Shows door fills through walls"
                default = false
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            stepslider {
                configName = "doorLineWidth"
                name = "Door Line Width"
                description = "Line width for doors"
                min = 1
                max = 5
                step = 1
                default = 3
                shouldShow { settings -> settings["boxWitherDoors"] as Boolean }
            }

            toggle {
                configName = "starMobBoxes"
                name = "Star Mob Boxes"
                description = "Highlights starred dungeon mobs and minibosses"
                default = false
            }

            colorpicker {
                configName = "starMobColor"
                name = "Starred Mob Color"
                description = "Outline color for normal starred mobs"
                default = Color(0, 255, 255, 255)
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            colorpicker {
                configName = "starMobChunkyColor"
                name = "Chunky Star Mob Color"
                description = "Withermancers, commanders, lords, and super archers"
                default = Color(255, 0, 128, 255)
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            colorpicker {
                configName = "starMobFelColor"
                name = "Fels Color"
                description = "Outline color for starred Fels"
                default = Color(0, 255, 128, 255)
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            colorpicker {
                configName = "starMobMiniColor"
                name = "Miniboss Color"
                description = "Outline color for minibosses"
                default = Color(235, 1, 165, 255)
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            colorpicker {
                configName = "starMobShadowAssassinColor"
                name = "Shadow Assassin Color"
                description = "Outline color for Shadow Assassins"
                default = Color(255, 0, 0, 255)
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            colorpicker {
                configName = "starMobSkeletonMasterColor"
                name = "Skeleton Master Color"
                description = "Outline color for Skeleton Masters"
                default = Color(255, 128, 0, 255)
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            stepslider {
                configName = "starMobLineWidth"
                name = "Star Mob Line Width"
                description = "Line width for starred mob boxes"
                min = 1
                max = 10
                step = 1
                default = 3
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            slider {
                configName = "starMobFillAlpha"
                name = "Star Mob Fill Alpha"
                description = "Fill opacity for starred mob boxes"
                min = 0f
                max = 1f
                default = 0f
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            toggle {
                configName = "starMobShowFullShadowAssassin"
                name = "Show Full Shadow Assassin"
                description = "Shows the full Shadow Assassin hitbox while invisible"
                default = true
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            toggle {
                configName = "starMobPhase"
                name = "Star Mob Through Walls"
                description = "Shows starred mob boxes through walls"
                default = false
                shouldShow { settings -> settings["starMobBoxes"] as Boolean }
            }

            toggle {
                configName = "separateMapInfo"
                name = "Separate map Info"
                description = "Renders the map info separate from the dungeon map"
                default = false
            }

            toggle {
                configName = "dungeonBreakdown"
                name = "Clear Breakdown"
                description = "Sends map info after run"
                default = false
            }
        }
    }

    category("Secrets") {
        subcategory("Waypoints", "secretWaypoints", "Renders Secret Waypoints") {
            toggle {
                configName = "secretWaypoints.text"
                name = "Waypoint Text"
                description = "Renders Secret Waypoints text"
                default = true
            }

            slider {
                configName = "secretWaypoints.textScale"
                name = "Text Scale"
                description = "Scale of the waypoint text"
                min = 0.1f
                max = 2f
                default = 1f
            }

            colorpicker {
                configName = "secretWaypointColor.redstonekey"
                name = "Redstone Key Color"
                description = "Highlight color for Redstone Key waypoints"
                default = Color(255, 0, 0, 255) // red
            }

            colorpicker {
                configName = "secretWaypointColor.wither"
                name = "Wither Color"
                description = "Highlight color for Wither waypoints"
                default = Color(0, 0, 255, 255) // blue
            }

            colorpicker {
                configName = "secretWaypointColor.bat"
                name = "Bat Color"
                description = "Highlight color for Bat waypoints"
                default = Color(128, 128, 128, 255) // gray
            }

            colorpicker {
                configName = "secretWaypointColor.item"
                name = "Item Color"
                description = "Highlight color for Item waypoints"
                default = Color(0, 255, 0, 255) // green
            }

            colorpicker {
                configName = "secretWaypointColor.chest"
                name = "Chest Color"
                description = "Highlight color for Chest waypoints"
                default = Color(255, 255, 0, 255) // yellow
            }

            colorpicker {
                configName = "secretWaypointColor.lever"
                name = "Lever Color"
                description = "Highlight color for Lever waypoints"
                default = Color(0, 255, 255, 255) // cyan
            }
        }

        subcategory("Routes","secretRoutes", "Enable rendering of route waypoints.") {
            toggle {
                configName = "secretRoutes.onlyRenderAfterClear"
                name = "Only After Clear"
                description = "Only show route waypoints after the room has been cleared."
                default = false
            }

            toggle {
                configName = "secretRoutes.stopRenderAfterGreen"
                name = "Stop After Green"
                description = "Stop rendering route waypoints once the room is marked green."
                default = false
            }

            textinput {
                configName = "secretRoutes.fileName"
                name = "File Name"
                description = "The name of the file to load the routes from (press reload after changing)"
                placeholder = "default.json"
            }

            button {
                configName = "secretRoutes.reload"
                name = "Reload Routes"
                description = "reloads the secret routes from the config file"

                onclick {
                    RouteRegistry.reload()
                }
            }

            keybind {
                configName = "secretRoutes.nextStep"
                name = "Next Step Bind"
                description = "Goes to the next step of a route"
                default = Zenith.Keys.R_BRACKET
            }

            keybind {
                configName = "secretRoutes.lastStep"
                name = "Last Step Bind"
                description = "Goes to the last step of a route"
                default = Zenith.Keys.L_BRACKET
            }

            keybind {
                configName = "secretRoutes.addCustom"
                name = "Custom Waypoint Bind"
                description = "Adds a custom waypoint to the route"
                default = Zenith.Keys.C
            }
        }

        subcategory("Route Rendering") {
            toggle {
                configName = "secretRoutes.text"
                name = "Waypoint Text"
                description = "Renders Secret Routes Waypoints text"
                default = true
            }

            toggle {
                configName = "secretRoutes.startEsp"
                name = "Start ESP"
                description = "Renders start text through walls"
                default = false
            }

            slider {
                configName = "secretRoutes.textScale"
                name = "Text Scale"
                description = "Scale of the waypoint text"
                min = 0.1f
                max = 2f
                default = 1f
            }

            colorpicker {
                configName = "secretRoutes.startColor"
                name = "Start Color"
                description = "Color for the starting point of a route."
                default = Color(0, 255, 0, 255) // green
            }

            colorpicker {
                configName = "secretRoutes.mineColor"
                name = "Mine Color"
                description = "Color for mining-related route waypoints."
                default = Color(255, 165, 0, 255) // orange
            }

            colorpicker {
                configName = "secretRoutes.superboomColor"
                name = "Superboom Color"
                description = "Color for Superboom TNT route waypoints."
                default = Color(255, 0, 0, 255) // red
            }

            colorpicker {
                configName = "secretRoutes.etherwarpColor"
                name = "Etherwarp Color"
                description = "Color for Etherwarp route waypoints."
                default = Color(0, 0, 255, 255) // blue
            }

            colorpicker {
                configName = "secretRoutes.pearlColor"
                name = "Pearl Color"
                description = "Color for Pearl waypoints."
                default = Color(0, 255, 255, 255) // blue
            }

            colorpicker {
                configName = "secretRoutes.chestColor"
                name = "Chest Color"
                description = "Color for Chest waypoints."
                default = Color(255, 255, 0, 255) // yellow
            }

            colorpicker {
                configName = "secretRoutes.itemColor"
                name = "Item Color"
                description = "Color for Item waypoints."
                default = Color(255, 255, 0, 255) // yellow
            }

            colorpicker {
                configName = "secretRoutes.essenceColor"
                name = "Essence Color"
                description = "Color for Essence waypoints."
                default = Color(255, 255, 0, 255) // yellow
            }

            colorpicker {
                configName = "secretRoutes.batColor"
                name = "Bat Color"
                description = "Color for bat route waypoints."
                default = Color(128, 128, 128, 255) // gray
            }

            colorpicker {
                configName = "secretRoutes.leverColor"
                name = "Lever Color"
                description = "Color for lever route waypoints."
                default = Color(0, 255, 255, 255) // cyan
            }
        }

        subcategory("Route Recording") {
            toggle {
                configName = "secretRoutes.recordingHud"
                name = "Recording Hud"
                description = "A helpful hud for recording secret routes"
            }

            toggle {
                configName = "secretRoutes.recordingHud.minimized"
                name = "Minimize Hud"
                description = "Makes the hud A lot smaller"
            }

            button {
                configName = "secretRoutes.startRecording"
                name = "Start Recording"
                description = "Starts recording a route (/aureon route start)"

                onclick {
                    RouteRecorder.startRecording()
                }
            }

            button {
                configName = "secretRoutes.stopRecording"
                name = "Stop Recording"
                description = "Stops recording a route (/aureon route stop)"

                onclick {
                    RouteRecorder.stopRecording()
                }
            }

            button {
                configName = "secretRoutes.saveRecording"
                name = "Save Recording"
                description =
                    "Saves the recording of the route (/aureon route save) (To change route file version do it in the file)"

                onclick {
                    RouteRecorder.saveRoute()
                }
            }
        }
    }

    category("Msc.") {
        subcategory("Block Overlay", "overlayEnabled", "Highlights the block you are looking at" ) {
            colorpicker {
                configName = "blockHighlightColor"
                name = "Highlight Color"
                description = "The color to highlight blocks"
                default = Color(0, 255, 255, 255)
            }

            toggle {
                configName = "fillBlockOverlay"
                name = "Fill blocks"
                description = "Fills the blocks with the color"
            }

            colorpicker {
                configName = "blockFillColor"
                name = "Fill Color"
                description = "The color to fill blocks"
                default = Color(0, 255, 255, 30)
                shouldShow { settings -> settings["fillBlockOverlay"] as Boolean }
            }

            toggle {
                configName = "overlayEntityHighlight"
                name = "Highlight Entities"
                description = "Highlights the entity you are looking at"
                default = false
            }

            toggle {
                configName = "overlayOutlinePhase"
                name = "Outline Through Walls"
                description = "Shows the outline through walls"
                default = false
            }

            toggle {
                configName = "overlayFillPhase"
                name = "Fill Through Walls"
                description = "Shows the fill through walls"
                default = false
                shouldShow { settings -> settings["fillBlockOverlay"] as Boolean }
            }

            toggle {
                configName = "overlayDynamicPhase"
                name = "Dynamic Phase"
                description = "First person renders through walls, third person uses normal depth"
                default = false
            }

            stepslider {
                configName = "overlayLineWidth"
                name = "Line width"
                description = "Line width for the outline"
                min = 1
                max = 5
                step = 1
                default = 3
            }
        }

        subcategory("Arrow Hitboxes", "arrowHitboxes", "Draws boxes around arrows") {
            colorpicker {
                configName = "arrowHitboxOutlineColor"
                name = "Outline Color"
                description = "Outline color for arrow hitboxes"
                default = Color(255, 255, 255, 255)
            }

            colorpicker {
                configName = "arrowHitboxFillColor"
                name = "Fill Color"
                description = "Fill color for arrow hitboxes"
                default = Color(255, 255, 255, 0)
            }

            stepslider {
                configName = "arrowHitboxLineWidth"
                name = "Line Width"
                description = "Line width for arrow hitboxes"
                min = 1
                max = 10
                step = 1
                default = 1
            }

            toggle {
                configName = "arrowHitboxPhase"
                name = "Through Walls"
                description = "Shows arrow hitboxes through walls"
                default = false
            }
        }

        subcategory("Inventory Buttons", "buttons", "Enables the inventory buttons") {
            button {
                configName = "buttons.edit"
                name = "Button Editor"
                description = "Opens the inventory button editor"
                placeholder = "Open"

                onclick {
                    client.setScreen(ButtonLayoutEditor())
                }
            }

            toggle {
                configName = "buttons.invOnly"
                name = "Inventory Only"
                description = "Only shows buttons in the inventory"
            }

            toggle {
                configName = "buttons.hideInTerms"
                name = "Clean Terminals"
                description = "Hides the buttons in various dungeon menus"
                default = true
            }
        }

        subcategory("Auto Friend", "autoFriend", "Automatically accepts Hypixel friend requests") {
            value = true

            toggle {
                configName = "autoFriend.hideRequest"
                name = "Hide Request"
                description = "Hides accepted friend request messages from chat"
                default = true
                shouldShow { settings -> settings["autoFriend"] as Boolean }
            }

            toggle {
                configName = "autoFriend.onlyOnHypixel"
                name = "Hypixel Only"
                description = "Only accepts friend requests when connected to Hypixel"
                default = true
                shouldShow { settings -> settings["autoFriend"] as Boolean }
            }
        }

        subcategory("Pet Display", "petDisplay", "Enables the pet display") {
            toggle {
                configName = "autopetMessages"
                name = "Announce AutoPet"
                description = "Announces when AutoPet Activates"
            }
        }

        subcategory("Health & Mana",  "bars", "Enables the health & mana bars") {
            toggle {
                configName = "bars.hideVanillaHealth"
                name = "Hide Health"
                description = "Hides the vanilla Minecraft health bar"
                default = false
            }

            toggle {
                configName = "bars.hideVanillaHunger"
                name = "Hide Hunger"
                description = "Hides the vanilla hunger display"
                default = false
            }

            toggle {
                configName = "bars.hideVanillaArmor"
                name = "Hide Armor"
                description = "Hides the vanilla armor display"
                default = false
            }

            toggle {
                configName = "bars.healthBar"
                name = "Health Bar"
                description = "Shows a custom health bar"
                default = false
            }

            toggle {
                configName = "bars.absorptionBar"
                name = "Absorption Bar"
                description = "Shows a custom absorption bar"
                default = false
                shouldShow { it["bars.healthBar"] as Boolean }
            }

            toggle {
                configName = "bars.hpChange"
                name = "HP Change HUD"
                description = "Shows the health delta (damage/healing numbers)"
                default = false
                shouldShow { it["bars.healthBar"] as Boolean }
            }

            toggle {
                configName = "bars.hpNum"
                name = "HP Number HUD"
                description = "Shows the numeric health value"
                default = false
                shouldShow { it["bars.healthBar"] as Boolean }
            }

            colorpicker {
                configName = "bars.healthColor"
                name = "Health Bar Color"
                description = "Color of the custom health bar"
                default = Color(255, 0, 0, 255)
                shouldShow { it["bars.healthBar"] as Boolean }
            }

            colorpicker {
                configName = "bars.absorptionColor"
                name = "Absorption Bar Color"
                description = "Color of the custom absorption bar"
                default = Color(255, 200, 0, 255)
                shouldShow { it["bars.absorptionBar"] as Boolean && it["bars.healthBar"] as Boolean }
            }

            toggle {
                configName = "bars.manaBar"
                name = "Mana Bar"
                description = "Shows a custom mana bar"
                default = false
            }

            toggle {
                configName = "bars.overflowManaBar"
                name = "OF Mana Bar"
                description = "Shows a custom overflow mana bar"
                default = false
                shouldShow { it["bars.manaBar"] as Boolean }
            }

            toggle {
                configName = "bars.ofMana"
                name = "OF Mana HUD"
                description = "Shows your overflow mana value"
                default = false
                shouldShow { it["bars.manaBar"] as Boolean }
            }

            toggle {
                configName = "bars.mpNum"
                name = "Mana Number HUD"
                description = "Shows the numeric mana value"
                default = false
                shouldShow { it["bars.manaBar"] as Boolean }
            }

            colorpicker {
                configName = "bars.manaColor"
                name = "Mana Bar Color"
                description = "Color of the custom mana bar"
                default = Color(0, 128, 255, 255)
                shouldShow { it["bars.manaBar"] as Boolean }
            }

            colorpicker {
                configName = "bars.ofmColor"
                name = "Overflow Mana Color"
                description = "Color of the overflow mana bar"
                default = Color(128, 0, 255, 255)
                shouldShow { it["bars.overflowManaBar"] as Boolean && it["bars.manaBar"] as Boolean }
            }
        }

        subcategory("Soulflow Display", "soulflowDisplay", "Enables the soulflow display")
        subcategory("Sword Blocking", "swordBlocking", "Enables 1.8.9 style sword blocking")
        subcategory("Auto Sprint", "autoSprint", "Automatically holds sprint while you play")

        subcategory("Cake Numbers", "cakeNumbers", "Shows cake years inside New Year Cake Bags") {
            colorpicker {
                configName = "cakeNumbers.color"
                name = "Text Color"
                description = "Text color for cake years"
                default = Color(85, 255, 255, 255)
            }
        }

        subcategory("Visual HUD") {
            toggle {
                configName = "fpsDisplay"
                name = "FPS Display"
                description = "Shows your current FPS on the HUD"
                default = false
            }

            colorpicker {
                configName = "fpsDisplay.color"
                name = "FPS Color"
                description = "Text color for the FPS display"
                default = Color(230, 114, 230, 255)
                shouldShow { it["fpsDisplay"] as Boolean }
            }

            toggle {
                configName = "cpsDisplay"
                name = "CPS Display"
                description = "Shows left and right clicks per second on the HUD"
                default = false
            }

            colorpicker {
                configName = "cpsDisplay.color"
                name = "CPS Color"
                description = "Text color for the CPS display"
                default = Color(230, 114, 230, 255)
                shouldShow { it["cpsDisplay"] as Boolean }
            }

            toggle {
                configName = "tpsDisplay"
                name = "TPS Display"
                description = "Shows estimated server TPS on the HUD"
                default = false
            }

            colorpicker {
                configName = "tpsDisplay.color"
                name = "TPS Color"
                description = "Text color for the TPS display"
                default = Color(230, 114, 230, 255)
                shouldShow { it["tpsDisplay"] as Boolean }
            }

            toggle {
                configName = "clockDisplay"
                name = "Clock Display"
                description = "Shows your local time on the HUD"
                default = false
            }

            colorpicker {
                configName = "clockDisplay.color"
                name = "Clock Color"
                description = "Text color for the clock display"
                default = Color(230, 114, 230, 255)
                shouldShow { it["clockDisplay"] as Boolean }
            }
        }

        /*
        subcategory("Custom Nametags") {
            toggle {
                configName = "customNametags"
                name = "Enabled"
                description = "Enables the soulflow display"
            }
        }
         */
    }
}
