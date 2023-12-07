package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.guild
import dev.erdragh.astralbot.textChannel
import dev.erdragh.astralbot.updatePresence
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
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Wrapper class around the [MinecraftServer] to provide convenience
 * methods for fetching [GameProfile]s
 * @author Erdragh
 */
class MinecraftHandler(private val server: MinecraftServer) : ListenerAdapter() {
    private val playerNames = HashSet<String>(server.maxPlayers)

    /**
     * Gets all currently online players' names
     * @return a [Collection] of all currently online players'
     * names
     */
    fun getOnlinePlayers(): Collection<String> {
        return playerNames
    }

    fun onPlayerJoin(name: String) = synchronized(playerNames) {
        playerNames.add(name)
        updatePresence(playerNames.size)
    }

    fun onPlayerLeave(name: String) = synchronized(playerNames) {
        playerNames.remove(name)
        updatePresence(playerNames.size)
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
    fun sendChatToDiscord(player: ServerPlayer?, message: String) {
        textChannel?.sendMessage(if (player != null) "<${player.displayName.string}> $message" else message)
            ?.setSuppressedNotifications(true)
            ?.setSuppressEmbeds(true)?.queue()
    }

    /**
     * Sends a Discord message into the Minecraft chat as a System Message
     * @param message the Discord message that will be put into the Minecraft chat
     */
    private fun sendDiscordToChat(message: Message) {
        sendFormattedMessage(message) {
            server.playerList.broadcastSystemMessage(DiscordMessageComponent(it), false)
        }
    }

    /**
     * Event handler that gets fired when the bot receives a message
     * @param event the event which contains information about the message
     */
    override fun onMessageReceived(event: MessageReceivedEvent) {
        // Only send messages from the configured channel and only if the author isn't a bot
        if (event.channel.idLong == textChannel?.idLong && !event.author.isBot) {
            sendDiscordToChat(event.message)
        }
    }

    /**
     * Formats a Discord [Member] into a [Component] with their
     * name and their Discord role color.
     * @param member the member which will be used to get the data
     */
    private fun formattedUser(member: Member): MutableComponent {
        return Component.literal(member.effectiveName).withStyle { it.withColor(member.colorRaw) }
    }

    /**
     * Formats a Discord [Message] into a [Component] that gets sent
     * into the Minecraft chat via the [send] argument
     *
     * This formatting entails the following:
     * - formatting users with [formattedUser]
     * - resolving replies
     * - the message itself
     * - if enabled in [AstralBotConfig], the embeds and attachments
     *   of the [message]
     *
     * @param message the relevant Discord message
     * @param send the function used to send the message into the Minecraft chat
     */
    private fun sendFormattedMessage(message: Message, send: (component: MutableComponent) -> Unit) {
        val comp = Component.empty()
        // The actual sender of the message
        message.member?.let {
            comp.append(formattedUser(it))
        }

        // Lambda that constructs the rest of the message. I'm doing it this way
        // because this may be used in a callback lambda below, where I can't return
        // out of sendFormattedMessage anymore
        val formatRestOfMessage: () -> MutableComponent = { ->
            val restOfMessage = Component.empty()
            // This is the actual message content
            val actualMessage = Component.empty()
                .append(Component.literal(": ").withStyle { it.withColor(ChatFormatting.GRAY) })
                .append(message.contentDisplay)
            // If it's enabled in the config you can click on a message and get linked to said message
            // in the actual Discord client
            if (AstralBotConfig.CLICKABLE_MESSAGES.get()) {
                actualMessage.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, message.jumpUrl)) }
            }
            restOfMessage.append(actualMessage)

            // Only adds embeds if it's enabled in the config
            if (AstralBotConfig.HANDLE_EMBEDS.get()) {
                restOfMessage.append(formatEmbeds(message))
            }

            restOfMessage
        }

        val referencedAuthor = message.referencedMessage?.author?.id

        if (referencedAuthor != null) {
            // This fetches the Member from the ID in a blocking manner
            guild?.retrieveMemberById(referencedAuthor)?.submit()?.whenComplete { member, error ->
                if (error != null) {
                    LOGGER.error("Failed to get member with id: $referencedAuthor", error)
                    return@whenComplete
                } else if (member == null) {
                    LOGGER.error("Failed to get member with id: $referencedAuthor")
                    return@whenComplete
                }
                comp.append(
                    Component.literal(" replying to ")
                        .withStyle { style -> style.withColor(ChatFormatting.GRAY).withItalic(true) })
                comp.append(formattedUser(member))
                comp.append(formatRestOfMessage())
                send(comp)
            }
        } else {
            comp.append(formatRestOfMessage())
            send(comp)
        }
    }

    /**
     * Formats the attachments and embeds on a Discord [Message] into
     * a comma separated list.
     * The following things can be en- or disabled in the [AstralBotConfig]:
     * - Making the embeds clickable
     * - Blocklist for disallowing certain URLs, blocking based on site hostname
     *
     * If an embed doesn't have a title it will be called embed`n` where `n` is
     * the index of the embed. All embeds come before attachments. If the URL of
     * an embed or attachment is blocked, it will display `BLOCKED` in red instead
     * of the name.
     *
     * @param message the message of which the attachments and embeds are handled
     */
    private fun formatEmbeds(message: Message): MutableComponent {
        val comp = Component.empty()
        // Adds a newline with space if there are embeds and the message isn't empty
        if (message.embeds.size + message.attachments.size > 0 && message.contentDisplay.isNotBlank()) comp.append("\n ")
        var i = 0
        message.embeds.forEach {
            if (i++ != 0) comp.append(", ")
            comp.append(formatEmbed(it.title ?: "embed$i", it.url))
        }
        message.attachments.forEach {
            if (i != 0) {
                comp.append(", ")
            } else {
                i++
            }
            comp.append(formatEmbed(it.fileName, it.url))
        }
        return comp
    }

    /**
     * Formats a single attachment with a name and URL
     *
     * The following things can be en- or disabled in the [AstralBotConfig]:
     * - Making the embeds clickable
     * - Blocklist for disallowing certain URLs, blocking based on site hostname
     *
     * If the URL of an embed or attachment is blocked, it will display
     * `BLOCKED` in red instead of the name.
     *
     * @param name the name of the embed
     * @param url the url of the embed
     */
    private fun formatEmbed(name: String, url: String?): MutableComponent {
        val comp = Component.empty()
        if (AstralBotConfig.urlAllowed(url)) {
            val embedComponent = Component.literal(name)
            if (AstralBotConfig.CLICKABLE_EMBEDS.get()) {
                embedComponent.withStyle { style ->
                    if (url != null && AstralBotConfig.CLICKABLE_EMBEDS.get()) {
                        style.withColor(ChatFormatting.BLUE).withUnderlined(true)
                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
                    } else style
                }
            }
            comp.append(embedComponent)
        } else {
            comp.append(Component.literal("BLOCKED").withStyle(ChatFormatting.RED))
        }
        return comp
    }
}