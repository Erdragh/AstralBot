package dev.erdragh.erdbot.forge

import dev.erdragh.erdbot.LOGGER
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod

@Mod("astralbot")
object BotMod {
    init {
        MinecraftForge.EVENT_BUS.register(object {

        })
        LOGGER.info("AstralBot Forge Started")
    }
}