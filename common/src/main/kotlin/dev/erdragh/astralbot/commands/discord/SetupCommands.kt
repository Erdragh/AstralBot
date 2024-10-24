package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.*
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.listeners.CommandHandlingListener
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * This command refreshes all registered commands on the server
 *
 * @author Erdragh
 */
object ReloadCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("reload", "Reloads the Discord Bot integrations (commands, etc.). This can take a while!")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        val guild = event.guild
        if (guild == null) {
            event.hook.setEphemeral(true).sendMessage(AstralBotTextConfig.GENERIC_ERROR.get()).queue()
            return
        }
        minecraftHandler?.updateWebhookClient()
        CommandHandlingListener.updateCommands(guild) { msg ->
            event.hook.setEphemeral(true).sendMessage(msg).queue()
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

            minecraftHandler?.updateWebhookClient()

            success = true
        }
        event.hook.setEphemeral(true)
            .sendMessage(if (success) AstralBotTextConfig.GENERIC_SUCCESS.get() else AstralBotTextConfig.GENERIC_ERROR.get())
            .queue()
    }
}

/**
 * This command sets up the Bot to give roles to people who link
 * their accounts. It also assigns the role to anybody who's already
 * linked themselves.
 *
 * It has the following options:
 * - `role`: The role which will be given to linked members
 *
 * @author Erdragh
 */
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
            event.hook.setEphemeral(true).sendMessage(AstralBotTextConfig.GENERIC_ERROR.get()).queue()
            LOGGER.error("Failed to resolve role from command option")
            return
        }

        AstralBotConfig.DISCORD_ROLE.set(role.idLong)
        AstralBotConfig.DISCORD_ROLE.save()

        waitForSetup()

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

        event.hook.setEphemeral(true).sendMessage(AstralBotTextConfig.GENERIC_SUCCESS.get()).queue()
    }
}