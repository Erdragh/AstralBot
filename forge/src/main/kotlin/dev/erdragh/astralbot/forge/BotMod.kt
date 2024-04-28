package dev.erdragh.astralbot.forge

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.commands.minecraft.registerMinecraftCommands
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.forge.event.SystemMessageEvent
import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import fuzs.forgeconfigapiport.forge.api.neoforge.v4.NeoForgeConfigRegistry
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod("astralbot")
object BotMod {
    init {
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, AstralBotConfig.SPEC)
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, AstralBotTextConfig.SPEC, "astralbot-text.toml")
        FORGE_BUS.addListener(::onServerStart)
        FORGE_BUS.addListener(::onServerStop)
        FORGE_BUS.addListener(::onChatMessage)
        FORGE_BUS.addListener(::onSystemMessage)
        FORGE_BUS.addListener(::onCommandRegistration)

        FORGE_BUS.addListener(::onPlayerJoin)
        FORGE_BUS.addListener(::onPlayerLeave)
    }

    private fun onServerStart(event: ServerStartedEvent) {
        LOGGER.info("AstralBot starting on Forge")
        startAstralbot(event.server)
    }

    private fun onServerStop(event: ServerStoppingEvent) {
        stopAstralbot()
    }

    private fun onChatMessage(event: ServerChatEvent) {
        minecraftHandler?.sendChatToDiscord(event.player, event.message)
    }

    private fun onSystemMessage(event: SystemMessageEvent) {
        if (event.message !is DiscordMessageComponent) {
            minecraftHandler?.sendChatToDiscord(null as ServerPlayer?, event.message)
        }
    }

    private fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        minecraftHandler?.onPlayerJoin(event.entity.name.string)
    }

    private fun onPlayerLeave(event: PlayerEvent.PlayerLoggedOutEvent) {
        minecraftHandler?.onPlayerLeave(event.entity.name.string)
    }

    private fun onCommandRegistration(event: RegisterCommandsEvent) {
        registerMinecraftCommands(event.dispatcher)
    }
}