package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.guild
import dev.erdragh.astralbot.textChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.net.URL
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
        val formattedMessage = formattedMessage(message)
        server.playerList.broadcastSystemMessage(formattedMessage, false)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.idLong == textChannel?.idLong && !event.author.isBot) {
            sendDiscordToChat(event.message)
        }
    }

    private fun formattedUser(member: Member): MutableComponent {
        return Component.literal(member.effectiveName).withStyle { it.withColor(member.colorRaw) }
    }

    private fun formattedMessage(message: Message): MutableComponent {
        val comp = Component.empty()
        message.member?.let {
            comp.append(formattedUser(it))
        }
        message.referencedMessage?.author?.id?.let { id ->
            guild?.retrieveMemberById(id)?.submit()?.get()?.let {
                comp.append(
                    Component.literal(" replying to ")
                        .withStyle { style -> style.withColor(ChatFormatting.GRAY).withItalic(true) })
                comp.append(formattedUser(it))
            }
        }
        val actualMessage = Component.empty()
            .append(Component.literal(": ").withStyle { it.withColor(ChatFormatting.GRAY) })
            .append(message.contentDisplay)
        if (AstralBotConfig.CLICKABLE_MESSAGES.get()) {
            actualMessage.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, message.jumpUrl)) }
        }
        comp.append(actualMessage)
        if (AstralBotConfig.HANDLE_EMBEDS.get()) {
            if (message.embeds.size + message.attachments.size > 0 && message.contentDisplay.isNotBlank()) comp.append("\n ")
            var i = 0
            message.embeds.forEach {
                if (i++ != 0) comp.append(", ")
                if (urlAllowed(it.url)) {
                    val embedComponent = Component.literal(it.title ?: "embed${i}")
                    if (AstralBotConfig.CLICKABLE_EMBEDS.get()) {
                        embedComponent.withStyle { style ->
                            it.url?.let { url ->
                                style.withColor(ChatFormatting.BLUE).withUnderlined(true)
                                    .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
                            }
                        }
                    }
                    comp.append(embedComponent)
                } else {
                    comp.append(Component.literal("URL BLOCKED").withStyle(ChatFormatting.RED))
                }
            }
            message.attachments.forEach {
                if (i != 0) {
                    comp.append(", ")
                } else {
                    i++
                }
                if (urlAllowed(it.url)) {
                    val embedComponent = Component.literal(it.fileName)
                    if (AstralBotConfig.CLICKABLE_EMBEDS.get()) {
                        embedComponent.withStyle { style ->
                            style.withColor(ChatFormatting.BLUE).withUnderlined(true)
                                .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, it.url))
                        }
                    }
                    comp.append(embedComponent)
                } else {
                    comp.append(Component.literal("URL BLOCKED").withStyle(ChatFormatting.RED))
                }
            }
        }

        return comp
    }

    private fun urlAllowed(url: String?): Boolean {
        if (url == null) return true
        try {
            val parsedURL = URL(url)
            for (blockedURL in AstralBotConfig.URL_BLOCKLIST.get()) {
                if (parsedURL.host.equals(URL(blockedURL).host)) return false
            }
        } catch (e: Exception) {
            LOGGER.warn("URL $url", e)
            return false
        }
        return true
    }
}