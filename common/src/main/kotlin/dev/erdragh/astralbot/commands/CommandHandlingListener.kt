package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.applicationId
import dev.erdragh.astralbot.guild
import dev.erdragh.astralbot.waitForSetup
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageCreateData

/**
 * Event listener for [JDA](https://jda.wiki) that sets up commands on servers
 *
 * @author Erdragh
 */
object CommandHandlingListener : ListenerAdapter() {
    /**
     * Registers commands on the Guild (Discord Server)
     * @param event the event that was fired when the bot
     * joined the server.
     */
    override fun onGuildJoin(event: GuildJoinEvent) {
        event.guild.updateCommands().addCommands(
            getEnabledCommands().map { it.command }
        ).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.contentDisplay == "!reload") {
            guild?.retrieveOwner()?.submit()?.whenComplete { owner, error ->
                if (error != null) {
                    LOGGER.error("Failed to retrieve Guild Owner", error)
                } else if (event.message.author.id == owner.id) {
                    if (guild != null) updateCommands(guild!!) { msg ->
                        event.channel.sendMessage(msg).queue()
                    }
                }
            }
        }
    }

    fun updateCommands(guild: Guild, sendMessage: (msg: String) -> Unit) {
        guild.retrieveCommands().submit().whenComplete { fetchedCommands, error ->
            if (error != null) {
                sendMessage("Something went wrong: ${error.localizedMessage}")
                return@whenComplete
            }
            waitForSetup()
            val deletedCommands = fetchedCommands.filter { it.applicationIdLong == applicationId }.map { guild.deleteCommandById(it.id).submit() }
            deletedCommands.forEach { it.get() }
            guild.updateCommands().addCommands(getEnabledCommands().map { it.command }).queue {
                sendMessage("Reloaded commands for guild")
            }
        }
    }

    /**
     * Handles the event that a user runs a slash command by
     * delegating it to the [HandledSlashCommand] implementations
     * @param event the interaction event that gets passed on
     * to the handlers
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val usedCommand = getEnabledCommands().find { it.command.name == event.name }
        if (usedCommand != null) {
            usedCommand.handle(event)
        } else {
            event.reply("Something went wrong").queue()
        }
    }

    /**
     * Handles the event that a user is writing a slash command
     * that has autocompletion by delegating the suggestions
     * to the [AutocompleteCommand] implementations.
     * @param event the event that gets passed on to the handlers
     */
    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val usedCommand =
            getEnabledCommands().find { it is AutocompleteCommand && it.command.name == event.name } as AutocompleteCommand?
        usedCommand?.autocomplete(event)
    }
}