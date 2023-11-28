package dev.erdragh.astralbot.forge

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.main
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod("astralbot")
object BotMod {
    init {
        FORGE_BUS.addListener(::onWorldLoad)
    }

    private fun onWorldLoad(event: LevelEvent.Load) {
        LOGGER.info("AstralBot starting on Forge")
        main()
    }
}