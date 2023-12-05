package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.guild
import dev.erdragh.astralbot.textChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Wrapper class around the [MinecraftServer] to provide convenience
 * methods for fetching [GameProfile]s
 * @author Erdragh
 */
class MinecraftHandler(private val server: MinecraftServer) : ListenerAdapter() {

    /**
     * Fetches all currently online players' [GameProfile]s
     * @return a [Collection] of all currently online players'
     * [GameProfile]s
     */
    fun getOnlinePlayers(): Collection<GameProfile> {
        return server.playerList.players.map { it.gameProfile }
    }

    /**
     * Fetches the [GameProfile] of a given Minecraft user ID
     * @param id a User ID possibly associated with a Minecraft Account
     * @return the [GameProfile] of the given [id] or `null`
     * if:
     * - The given [id] doesn't have an actual Minecraft account
     *   associated with it
     * - The [server]'s profile cache hasn't been initialized yet
     */
    fun byUUID(id: UUID): GameProfile? {
        return server.profileCache?.get(id)?.getOrNull()
    }

    /**
     * Fetches the [GameProfile] of a given Minecraft username
     * @param name a username possibly associated with a Minecraft Account
     * @return the [GameProfile] of the given [name] or `null`
     * if:
     * - The given [name] doesn't have an actual Minecraft account
     *   associated with it
     * - The [server]'s profile cache hasn't been initialized yet
     */
    fun byName(name: String): GameProfile? {
        return server.profileCache?.get(name)?.getOrNull()
    }

    /**
     * Sends a message into the configured Discord channel based on
     * the Chat [message] the [player] sent.
     * @param player the Player who sent the message
     * @param message the String contents of the message
     */
    fun sendChatToDiscord(player: ServerPlayer, message: String) {
        textChannel?.sendMessage("<${player.name.string}> $message")?.setSuppressedNotifications(true)
            ?.setSuppressEmbeds(true)?.queue()
    }

    private fun sendDiscordToChat(message: Message) {
        val color = guild?.getMemberById(message.author.idLong)?.colorRaw
        val formattedMessage =
            Component.literal(message.author.effectiveName).withStyle { it.withColor(color ?: 0xffffff) }.append(": ")
                .append(message.contentDisplay)
        server.playerList.broadcastSystemMessage(formattedMessage, false)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.idLong == textChannel?.idLong) {
            sendDiscordToChat(event.message)
        }
    }
}