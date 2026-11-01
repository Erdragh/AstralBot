package dev.erdragh.astralbot.handlers

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.util.*
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.text.DecimalFormat
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

class MinecraftHandler(private val server: MinecraftServer) : ListenerAdapter() {
    private val playerNames = HashSet<String>(server.maxPlayers)
    private val notchPlayer = byName("Notch")?.let {
        ServerPlayer(
            this.server,
            this.server.allLevels.elementAt(0),
            it,
            ClientInformation.createDefault()
        )
    }
    private var webhookClient: WebhookClient? = null

    fun updateWebhookClient() {
        val url = AstralBotConfig.WEBHOOK_URL.get()

        if (webhookClient == null || url != webhookClient?.url) {
            webhookClient = url.let {
                if (it != "") WebhookClient.withUrl(it) else null
            }
        }

        if (webhookClient == null) {
            textChannel?.retrieveWebhooks()?.submit()?.whenComplete { webhooks, error ->
                if (error != null) {
                    LOGGER.error(
                        "Failed to fetch webhooks for channel: ${textChannel!!.id} (${textChannel!!.name})",
                        error
                    )
                } else {
                    if (webhooks.size > 1) {
                        LOGGER.warn(
                            "Multiple webhooks available for channel: ${textChannel!!.id} (${textChannel!!.name}). " +
                                "AstralBot will use the first one. If you want to use a specific one, " +
                                "set the webhook.url config option."
                        )
                        webhookClient = JDAWebhookClient.from(webhooks[0])
                    } else if (webhooks.size == 1) {
                        webhookClient = JDAWebhookClient.from(webhooks[0])
                    } else {
                        LOGGER.info(
                            "No webhook found in channel: ${textChannel!!.id} (${textChannel!!.name}). " +
                                "AstralBot will try to create one."
                        )

                        textChannel!!.createWebhook("AstralBot Chat synchronization")
                            .submit()
                            .whenComplete { webhook, createError ->
                                if (createError != null) {
                                    LOGGER.error(
                                        "Failed to create webhook for channel: " +
                                            "${textChannel!!.id} (${textChannel!!.name})",
                                        createError
                                    )
                                } else {
                                    webhookClient = JDAWebhookClient.from(webhook)
                                }
                            }
                    }
                }
            }
        }
    }

    companion object {
        private val numberFormat = DecimalFormat("###.##")
    }

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

    fun stopServer() {
        server.halt(false)
    }

    fun tickReport(): String {
        return AstralBotTextConfig.TICK_REPORT.get()
            .replace(
                "{{mspt}}",
                numberFormat.format(server.currentSmoothedTickTime)
            )
            .replace(
                "{{tps}}",
                numberFormat.format(
                    min(
                        20.0,
                        1000.0 / server.currentSmoothedTickTime
                    )
                )
            )
    }

    fun byUUID(id: UUID): GameProfile? {
        return server.profileCache?.get(id)?.getOrNull()
    }

    fun byName(name: String): GameProfile? {
        return server.profileCache?.get(name)?.getOrNull()
    }

    fun sendChatToDiscord(player: ServerPlayer?, message: Component) {
        if (shuttingDown.get()) return

        val attachments = message.toFlatList().mapNotNull {
            it.style.hoverEvent
        }

        val formattedEmbeds: MutableList<MessageEmbed> = mutableListOf()
        val items: MutableList<ItemStack> = mutableListOf()

        for (attachment in attachments) {
            attachment.getValue(HoverEvent.Action.SHOW_TEXT)?.let {
                formattedEmbeds.add(formatHoverText(it))
            }

            attachment.getValue(HoverEvent.Action.SHOW_ITEM)?.itemStack?.let {
                formatHoverItems(it, items,
