package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.startAstralbot
import dev.erdragh.astralbot.stopAstralbot
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.util.concurrent.atomic.AtomicBoolean

object BotMod : ModInitializer {
    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register {
            LOGGER.info("Starting AstralBot on Fabric")
            startAstralbot(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            stopAstralbot()
        }
    }
}