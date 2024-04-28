package dev.erdragh.astralbot.forge

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.commands.minecraft.registerMinecraftCommands
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.forge.event.SystemMessageEvent
import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent

@Mod("astralbot")
class BotMod(eventBus: IEventBus) {
    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AstralBotConfig.SPEC)
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AstralBotTextConfig.SPEC, "astralbot-text.toml")
        eventBus.addListener(::onServerStart)
        eventBus.addListener(::onServerStop)
        eventBus.addListener(::onChatMessage)
        eventBus.addListener(::onSystemMessage)
        eventBus.addListener(::onCommandRegistration)

        eventBus.addListener(::onPlayerJoin)
        eventBus.addListener(::onPlayerLeave)
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