package dev.erdragh.astralbot.fabric.event

import dev.erdragh.astralbot.fabric.event.ServerMessageEvents.ChatMessage
import dev.erdragh.astralbot.fabric.event.ServerMessageEvents.GameMessage
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.server.level.ServerPlayer

class ServerMessageEvents {
    companion object {
        @JvmStatic
        val CHAT_MESSAGE: Event<ChatMessage> =
            EventFactory.createArrayBacked(ChatMessage::class.java) { callbacks -> ChatMessage { m, p, _ -> run {
                var message = m
                var player: ServerPlayer? = null
                if (m is TranslatableComponent) {
                    if (m.args[1] !is Component) {
                        message = TextComponent(m.args[1] as String)
                        player = p
                    }
                }

                for (callback in callbacks) {
                    callback.onChatMessage(message, player, null);
                }}}
            }

        @JvmStatic
        val GAME_MESSAGE: Event<GameMessage> =
            EventFactory.createArrayBacked(GameMessage::class.java) { callbacks -> GameMessage { _, message, _ -> run {
                for (callback in callbacks) {
                    callback.onGameMessage(null, message, null);
                }}}
            }
    }

    fun interface ChatMessage {
        fun onChatMessage(message: Component, player: ServerPlayer?, unused: Void?)
    }

    fun interface GameMessage {
        fun onGameMessage(unused1: Void?, message: Component, unused2: Void?)
    }
}