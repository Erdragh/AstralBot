package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.setupAstralbot
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import java.util.concurrent.atomic.AtomicBoolean

object BotMod : ModInitializer {
    private val LOADED = AtomicBoolean(false)
    override fun onInitialize() {
        ServerWorldEvents.LOAD.register { server, _ ->
            if (!LOADED.getAndSet(true)) {
                LOGGER.info("Starting AstralBot on Fabric")
                setupAstralbot(server)
            }
        }
    }
}