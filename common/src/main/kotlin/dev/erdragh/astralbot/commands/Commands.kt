// This file contains multiple commands which don't warrant separate files

package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.guild
import dev.erdragh.astralbot.handlers.FAQHandler
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.minecraftHandler
import dev.erdragh.astralbot.textChannel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
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

/**
 * This command refreshes all registered commands on the server
 *
 * @author Erdragh
 */
object RefreshCommandsCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("reload", "Reloads the Discord Bot integrations (commands, etc.)")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))

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

        val list = minecraftHandler?.getOnlinePlayers()?.map { "- ${it}\n" }

        if (!list.isNullOrEmpty()) {
            event.hook.sendMessage("The following players are currently online:\n${list.joinToString()}").queue()
        } else {
            event.hook.sendMessage("There are no players online currently").queue()
        }
    }
}

/**
 * This command sets up the channel and guild in the
 * [AstralBotConfig] for the chat synchronization.
 *
 * It has the following options:
 * - `channel`: (Optional) the channel where the chat messages
 *   will be synchronized. If this option is not provided, the
 *   channel in which the command was executed will be used
 *   instead.
 *
 * @author Erdragh
 */
object ChatSyncCommand : HandledSlashCommand {
    private const val OPTION_CHANNEL = "channel"
    override val command: SlashCommandData =
        Commands.slash("chatsync", "Configures the Bot to synchronize chat messages").addOption(
            OptionType.CHANNEL,
            OPTION_CHANNEL,
            "The channel where to sync to. If this isn't provided the current channel will be used",
            false
        )
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        var success = false
        runBlocking {
            val eventChannel = event.channel
            val g = event.guild
            val channel = event.getOptionsByType(OptionType.CHANNEL).findLast { it.name == OPTION_CHANNEL }?.asChannel
            textChannel = if (channel is TextChannel) {
                channel
            } else if (eventChannel is TextChannel) {
                eventChannel
            } else return@runBlocking

            AstralBotConfig.DISCORD_CHANNEL.set(textChannel!!.idLong)
            AstralBotConfig.DISCORD_CHANNEL.save()

            guild = g ?: return@runBlocking

            AstralBotConfig.DISCORD_GUILD.set(guild!!.idLong)
            AstralBotConfig.DISCORD_GUILD.save()
            success = true
        }
        event.hook.setEphemeral(true)
            .sendMessage(if (success) "Successfully set up chat synchronization" else "Something went wrong while setting up chat sync")
            .queue()
    }
}

object LinkRoleCommand : HandledSlashCommand {
    private const val OPTION_ROLE = "role"
    override val command: SlashCommandData =
        Commands.slash("linkrole", "Configures the Bot to assign a role to linked accounts")
            .addOption(OptionType.ROLE, OPTION_ROLE, "The role which will be given to linked members", true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER, Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS))

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        val role = event.getOptionsByType(OptionType.ROLE).findLast { it.name == OPTION_ROLE }?.asRole
        if (role !is Role) {
            event.hook.setEphemeral(true).sendMessage("Failed to resolve role from command option").queue()
            LOGGER.error("Failed to resolve role from command option")
            return
        }

        AstralBotConfig.DISCORD_ROLE.set(role.idLong)
        AstralBotConfig.DISCORD_ROLE.save()

        for (id in WhitelistHandler.getAllUsers()) {
            guild?.retrieveMemberById(id)?.submit()?.whenComplete { member, error ->
                if (error != null) {
                    LOGGER.error("Failed to get member with id: $id", error)
                    return@whenComplete
                } else if (member == null) {
                    LOGGER.error("Failed to get member with id: $id")
                    return@whenComplete
                }
                try {
                    guild?.addRoleToMember(member, role)?.queue()
                } catch (e: Exception) {
                    LOGGER.error("Failed to add role ${role.name} to member ${member.effectiveName}", e)
                }
            }
        }

        event.hook.setEphemeral(true).sendMessage("Set up role ${role.name} for linked members").queue()
    }
}