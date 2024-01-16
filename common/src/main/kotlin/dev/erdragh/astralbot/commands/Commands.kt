// This file contains multiple commands which don't warrant separate files

package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * Array containing every single command AstralBot supports
 */
val allCommands = arrayOf(
    // Bot management commands
    ReloadCommand,
    // Utility Commands
    FAQCommand,
    // Player related commands
    LinkCommand,
    UnlinkCommand,
    LinkCheckCommand,
    ListCommand,
    // Bot setup commands
    ChatSyncCommand,
    LinkRoleCommand,
    // Minecraft server management commands
    UptimeCommand,
    StopCommand,
    TPSCommand,
    UsageCommand
)

/**
 * Filters the list of [allCommands] based on the commands enabled
 * in the [AstralBotConfig]
 * @see AstralBotConfig.ENABLED_COMMANDS
 * @see allCommands
 * @return filtered list containing only the commands that are enabled
 */
fun getEnabledCommands(): Collection<HandledSlashCommand> {
    return allCommands.filter { AstralBotConfig.ENABLED_COMMANDS.get().contains(it.command.name) }
}

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

/**
 * Base class for commands that need autocompletion on Minecraft usernames
 * @author Erdragh
 */
abstract class MinecraftUserAutocompleteCommand(private vararg val options: String) : AutocompleteCommand {
    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (options.contains(event.focusedOption.name)) {
            event.replyChoiceStrings(
                minecraftHandler?.getOnlinePlayers()?.filter { it.startsWith(event.focusedOption.value) } ?: listOf())
                .queue()
        }
    }
}