package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.commands.minecraft.registerMinecraftCommands
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer
import net.neoforged.fml.config.ModConfig

object BotMod : ModInitializer {
    override fun onInitialize() {
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, AstralBotConfig.SPEC)
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, AstralBotTextConfig.SPEC, "astralbot-text.toml")

        ServerLifecycleEvents.SERVER_STARTED.register {
            LOGGER.info("Starting AstralBot on Fabric")
            startAstralbot(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            stopAstralbot()
        }

        ServerMessageEvents.CHAT_MESSAGE.register { message, player, _ ->
            minecraftHandler?.sendChatToDiscord(player, message.decoratedContent())
        }
        ServerMessageEvents.GAME_MESSAGE.register { _, message, _ ->
            if (message !is DiscordMessageComponent) {
                minecraftHandler?.sendChatToDiscord(null as ServerPlayer?, message)
            }
        }
        ServerMessageEvents.COMMAND_MESSAGE.register { message, _, _ ->
            val content = message.decoratedContent()
            if (content !is DiscordMessageComponent) {
                minecraftHandler?.sendChatToDiscord(null as ServerPlayer?, content)
            }
        }

        ServerPlayConnectionEvents.JOIN.register { packet, _, _ ->
            minecraftHandler?.onPlayerJoin(packet.player.name.string)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { packet, _ ->
            minecraftHandler?.onPlayerLeave(packet.player.name.string)
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerMinecraftCommands(dispatcher)
        }
    }
}