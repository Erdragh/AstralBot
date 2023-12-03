// This file contains multiple commands which don't warrant separate files

package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.handlers.FAQHandler
import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
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
    ListCommand
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

/**
 * This command refreshes all registered commands on the server
 *
 * @author Erdragh
 */
object RefreshCommandsCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("reload", "Reloads the Discord Bot integrations (commands, etc.)")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        val guild = event.guild
        if (guild == null) {
            event.hook.setEphemeral(true).sendMessage("Failed to fetch Guild to refresh").queue()
            return
        }
        guild.updateCommands().addCommands(commands.map { it.command }).queue {
            event.hook.setEphemeral(true).sendMessage("Reloaded commands for guild").queue()
        }
    }
}

/**
 * This command prints FAQ messages, fetching them from
 * disk using the [FAQHandler].
 *
 * It has the following options:
 * - `id`: used to fetch the FAQ from disk
 *
 * @author Erdragh
 */
object FAQCommand : HandledSlashCommand, AutocompleteCommand {
    private const val OPTION_ID = "id"

    override val command: SlashCommandData = Commands.slash("faq", "prints a specified FAQ answer")
        .addOption(OptionType.STRING, OPTION_ID, "id of the faq", true, true)

    override fun handle(event: SlashCommandInteractionEvent) {
        // since interacting with the disk may take a while, the reply gets deferred
        event.deferReply(false).queue()

        val faqId = event.getOption(OPTION_ID)!!.asString
        val faqResponse = FAQHandler.getFAQForId(faqId)

        event.hook.sendMessage(faqResponse).queue()
    }

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == OPTION_ID) {
            event.replyChoiceStrings(FAQHandler.suggestFAQIds(event.focusedOption.value)).queue()
        }
    }
}

/**
 * This command lists all currently online players
 *
 * @author Erdragh
 */
object ListCommand : HandledSlashCommand {
    override val command: SlashCommandData = Commands.slash("list", "Lists currently online players")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        val list = minecraftHandler?.getOnlinePlayers()?.map { "- ${it.name}\n" }

        if (!list.isNullOrEmpty()) {
            event.hook.sendMessage("The following players are currently online:\n${list.joinToString()}").queue()
        } else {
            event.hook.sendMessage("There are no players online currently").queue()
        }
    }
}