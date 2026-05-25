package com.kaelith.aureon

import com.kaelith.aureon.managers.FeatureManager
import com.kaelith.aureon.api.animation.DeltaTracker
import com.kaelith.aureon.api.nvg.NVGPIPRenderer
import com.kaelith.aureon.events.EventBus
import com.kaelith.aureon.events.core.TickEvent
import com.kaelith.aureon.utils.config
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlinx.coroutines.*
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry

object AureonCore: ClientModInitializer {
    @JvmStatic val LOGGER: Logger = LogManager.getLogger("aureon")
    @JvmStatic val NAMESPACE: String = "aureon"
    @JvmStatic val PREFIX: String = "§7[§dAureon§7]"
    @JvmStatic val SHORTPREFIX: String = "§d[A]"
    @JvmStatic val ETHER: String = "https://ether.stellarskys.co"
    @JvmStatic val API: String = "https://api.stellarskys.co"
    @JvmStatic val PATH: String get() = config.path
    @JvmStatic val DELTA: DeltaTracker = DeltaTracker()
    @JvmStatic val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onInitializeClient() {
        FeatureManager.loadFeatures()
        FeatureManager.initializeFeatures()

        PictureInPictureRendererRegistry.register { NVGPIPRenderer(it.bufferSource()) }
    }
}
