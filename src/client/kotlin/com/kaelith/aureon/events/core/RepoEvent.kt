package com.kaelith.aureon.events.core

import com.kaelith.aureon.api.events.Event
import tech.thatgravyboat.repolib.api.RepoStatus

object RepoEvent {
    class Success: Event()
    class Sataus(val status: RepoStatus): Event()
}