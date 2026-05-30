package com.kaelith.aureon.features.msc

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Command
import com.kaelith.aureon.api.handlers.Atlas
import com.kaelith.aureon.api.handlers.Signal

@Command
object AutoFriendCommand : Atlas("autofriend") {
    init {
        runs {
            val enabled = AutoFriend.toggle()
            Signal.fakeMessage("${AureonCore.PREFIX} \u00a7bAuto Friend: ${if (enabled) "\u00a7aON" else "\u00a7cOFF"}")
        }
    }
}
