package dev.erdragh.astralbot.forge

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.main
import net.minecraftforge.fml.common.Mod

@Mod("astralbot")
object BotMod {
    init {
        LOGGER.info("Starting AstralBot on Forge")
        main()
    }
}