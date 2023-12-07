package dev.erdragh.astralbot.fabric

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.config.AstralBotConfig
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraftforge.fml.config.ModConfig

object BotMod : ModInitializer {
    override fun onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, AstralBotConfig.SPEC)

        ServerLifecycleEvents.SERVER_STARTED.register {
            LOGGER.info("Starting AstralBot on Fabric")
            startAstralbot(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            stopAstralbot()
        }

        ServerMessageEvents.CHAT_MESSAGE.register { message, player, _ ->
            minecraftHandler?.sendChatToDiscord(player, message.signedContent())
        }

        ServerPlayConnectionEvents.JOIN.register { packet, _, _ ->
            minecraftHandler?.onPlayerJoin(packet.player.name.string)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { packet, _ ->
            minecraftHandler?.onPlayerLeave(packet.player.name.string)
        }
    }
}