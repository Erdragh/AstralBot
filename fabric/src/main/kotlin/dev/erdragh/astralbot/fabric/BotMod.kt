package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.fabric.event.ServerMessageEvents
import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.api.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig

object BotMod : ModInitializer {
    override fun onInitialize() {
        ModLoadingContext.registerConfig(MODID, ModConfig.Type.SERVER, AstralBotConfig.SPEC)

        ServerLifecycleEvents.SERVER_STARTED.register {
            LOGGER.info("Starting AstralBot on Fabric")
            startAstralbot(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            stopAstralbot()
        }

        ServerMessageEvents.CHAT_MESSAGE.register { message, player, _ ->
            minecraftHandler?.sendChatToDiscord(player, message.string)
        }
        ServerMessageEvents.GAME_MESSAGE.register { _, message, _ ->
            if (message !is DiscordMessageComponent) {
                minecraftHandler?.sendChatToDiscord(null as ServerPlayer?, message.string)
            }
        }

        ServerPlayConnectionEvents.JOIN.register { packet, _, _ ->
            minecraftHandler?.onPlayerJoin(packet.player.name.string)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { packet, _ ->
            minecraftHandler?.onPlayerLeave(packet.player.name.string)
        }
    }
}