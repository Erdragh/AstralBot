package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.main
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents

object BotMod : ModInitializer {
    override fun onInitialize() {
        ServerWorldEvents.LOAD.register { _, _ ->
            LOGGER.info("Starting AstralBot on Fabric")
            main()
        }
    }
}