package dev.erdragh.erdbot.commands

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandHandlingListener : ListenerAdapter() {

    override fun onGuildJoin(event: GuildJoinEvent) {
        event.guild.updateCommands().addCommands(
            commands.map { it.command }
        ).queue()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val usedCommand = commands.find { it.command.name == event.name }
        if (usedCommand != null) {
            usedCommand.handle(event)
        } else {
            event.reply("Something went wrong").queue()
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val usedCommand = commands.find { it is AutocompleteCommand && it.command.name == event.name } as AutocompleteCommand?
        usedCommand?.autocomplete(event)
    }
}