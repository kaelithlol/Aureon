package com.kaelith.aureon.features.dungeons

import com.kaelith.aureon.annotations.Command
import com.kaelith.aureon.api.handlers.Signal
import com.kaelith.aureon.api.handlers.Atlas

@Command
object FunnyCommand: Atlas("dn") {
    init {
        runs {
            Signal.sendCommand("/warp dungeon_hub")
            Signal.fakeMessage("§7Warping to...")
            Signal.fakeMessage("§7Deez nuts lmao")
        }
    }
}