package dev.erdragh.erdbot.forge

import dev.erdragh.erdbot.LOGGER
import dev.erdragh.erdbot.main
import net.minecraftforge.fml.common.Mod

@Mod("astralbot")
object BotMod {
    init {
        LOGGER.info("Starting AstralBot on Forge")
        main()
    }
}