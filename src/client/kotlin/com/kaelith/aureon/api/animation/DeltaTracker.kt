package com.kaelith.aureon.api.animation

import com.kaelith.aureon.api.handlers.Chronos
import com.kaelith.aureon.api.handlers.Chronos.millis

class DeltaTracker(targetFps: Float = 120f) {
    private var tracker = Chronos.now
    private val msPerFrame = 1000.0 / targetFps

    var currentDelta = 0.0
        private set

    /**
     * Calculates how many "target frames" have passed since the last call.
     * Use this in your Animation class.
     */
    fun getDelta(): Double {
        val elapsed = tracker.since.millis.toDouble()
        tracker = Chronos.now
        return elapsed / msPerFrame
    }

    fun updateDelta() {
        currentDelta = getDelta()
    }
}