package dev.erdragh.astralbot.forge

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.setupAstralbot
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.util.concurrent.atomic.AtomicBoolean

@Mod("astralbot")
object BotMod {
    private val LOADED = AtomicBoolean(false)
    init {
        FORGE_BUS.addListener(::onWorldLoad)
    }

    private fun onWorldLoad(event: LevelEvent.Load) {
        if (!LOADED.getAndSet(true)) {
            LOGGER.info("AstralBot starting on Forge")
            val server = event.level.server
            if (server == null) {
                throw IllegalStateException("No server accessible onWorldLoad")
            } else {
                setupAstralbot(server)
            }
        }
    }
}