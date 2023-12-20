// This file contains multiple commands which don't warrant separate files

package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * Array of all commands the bot provides.
 * This gets used to register the commands.
 */
val commands = arrayOf(
    RefreshCommandsCommand,
    FAQCommand,
    LinkCommand,
    UnlinkCommand,
    LinkCheckCommand,
    ListCommand,
    ChatSyncCommand,
    LinkRoleCommand
)

/**
 * Interface to make a common denominator for
 * the implementation and management of commands.
 *
 * This connects the command instantiation and
 * its handling
 * @author Erdragh
 */
interface HandledSlashCommand {
    /** The command data containing name, options, etc. */
    val command: SlashCommandData

    /**
     * Handler for when the command gets executed
     * @param event the event that is connected with the
     * issuing of the command
     */
    fun handle(event: SlashCommandInteractionEvent)
}

/**
 * Interface to make a common denominator for
 * autocomplete commands
 * @author Erdragh
 */
fun interface AutocompleteCommand {
    /**
     * Function responsible for providing autocomplete suggestions
     * to the given [event]
     * @param event the event where this function provides suggestions
     */
    fun autocomplete(event: CommandAutoCompleteInteractionEvent)
}

abstract class MinecraftUserAutocompleteCommand(private vararg val options: String) : AutocompleteCommand {
    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (options.contains(event.focusedOption.name)) {
            event.replyChoiceStrings(
                minecraftHandler?.getOnlinePlayers()?.filter { it.startsWith(event.focusedOption.value) } ?: listOf())
                .queue()
        }
    }
}