package dev.erdragh.erdbot.fabric

import dev.erdragh.erdbot.LOGGER
import dev.erdragh.erdbot.main
import net.fabricmc.api.ModInitializer

object BotMod : ModInitializer {
    override fun onInitialize() {
        LOGGER.info("Starting AstralBot on Fabric")
        main()
    }
}