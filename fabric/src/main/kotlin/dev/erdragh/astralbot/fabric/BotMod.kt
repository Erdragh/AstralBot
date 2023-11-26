package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.main
import net.fabricmc.api.ModInitializer

object BotMod : ModInitializer {
    override fun onInitialize() {
        LOGGER.info("Starting AstralBot on Fabric")
        main()
    }
}