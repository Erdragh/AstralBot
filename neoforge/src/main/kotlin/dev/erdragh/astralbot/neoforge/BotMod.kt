package dev.erdragh.astralbot.neoforge

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.commands.minecraft.registerMinecraftCommands
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.neoforge.event.SystemMessageEvent
import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import dev.erdragh.astralbot.neoforge.event.CommandMessageEvent
import net.minecraft.server.level.ServerPlayer
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS

@Mod("astralbot")
object BotMod {
    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AstralBotConfig.SPEC)
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AstralBotTextConfig.SPEC, "astralbot-text.toml")
        FORGE_BUS.addListener(::onServerStart)
        FORGE_BUS.addListener(::onServerStop)
        FORGE_BUS.addListener(::onChatMessage)
        FORGE_BUS.addListener(::onSystemMessage)
        FORGE_BUS.addListener(::onCommandMessage)
        FORGE_BUS.addListener(::onCommandRegistration)

        FORGE_BUS.addListener(::onPlayerJoin)
        FORGE_BUS.addListener(::onPlayerLeave)
    }

    private fun onServerStart(event: ServerStartedEvent) {
        LOGGER.info("AstralBot starting on NeoForge")
        startAstralbot(event.server)
    }

    // Unused parameter suppressed to keep type
    // information about Server stop event.
    @Suppress("UNUSED_PARAMETER")
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

    private fun onCommandMessage(event: CommandMessageEvent) {
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