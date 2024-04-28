package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.guild
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.minecraftHandler
import dev.erdragh.astralbot.waitForSetup
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * This command links a Discord user to a Minecraft account.
 *
 * It has the following options:
 * - `code`: the link code the user got from the screen trying to log into the server
 *
 * @author Erdragh
 */
object LinkCommand : HandledSlashCommand {
    // Specifying option names as constants to prevent typos
    private const val OPTION_CODE = "code"

    private const val UNNAMED_ACCOUNT = "Unnamed Account";

    override val command: SlashCommandData =
        Commands.slash("link", "Links your Minecraft account with your Discord account")
            .addOption(OptionType.NUMBER, OPTION_CODE, "your personal link code", true)

    override fun handle(event: SlashCommandInteractionEvent) {
        // DB Interactions could take a while, so the reply needs to get deferred
        event.deferReply(true).queue()

        // Get the Minecraft user ID from the link code
        val linkCode = event.getOption(OPTION_CODE)?.asString
        val linkCodeInt = linkCode?.toDoubleOrNull()?.toInt()
        val minecraftID = if (linkCodeInt != null) WhitelistHandler.getPlayerFromCode(linkCodeInt) else null

        // Notify the user that the given link code couldn't be associated with a Minecraft account
        if (minecraftID == null) {
            event.hook.setEphemeral(true).sendMessage(AstralBotTextConfig.LINK_NO_MINECRAFT.get()).queue()
            return
        }

        // Gets the user profile of the given Minecraft user ID to get access to the name
        val minecraftUser = minecraftHandler?.byUUID(minecraftID)

        try {
            // Depending on the whitelisting status of the given data send the relevant response
            if (WhitelistHandler.checkWhitelist(minecraftID) != null) {
                event.hook.setEphemeral(true)
                    .sendMessageFormat(
                        AstralBotTextConfig.LINK_MINECRAFT_TAKEN.get()
                            .replace("{{name}}", minecraftUser?.name ?: UNNAMED_ACCOUNT)
                    )
                    .queue()
            } else if (WhitelistHandler.checkWhitelist(event.user.idLong) != null) {
                event.hook.setEphemeral(true).sendMessageFormat(
                    AstralBotTextConfig.LINK_DISCORD_TAKEN.get()
                        .replace("{{name}}", event.member?.asMention ?: UNNAMED_ACCOUNT)
                ).queue()
            } else {
                WhitelistHandler.whitelist(event.user, minecraftID)
                waitForSetup()
                guild?.getRoleById(AstralBotConfig.DISCORD_ROLE.get())?.let { role ->
                    try {
                        guild?.addRoleToMember(event.user, role)?.queue()
                    } catch (e: Exception) {
                        LOGGER.error("Failed to add role ${role.name} to member ${event.user.asMention}", e)
                    }
                }
                event.hook.setEphemeral(true)
                    .sendMessageFormat(
                        AstralBotTextConfig.LINK_SUCCESSFUL.get()
                            .replace("{{dc}}", event.member?.asMention ?: UNNAMED_ACCOUNT)
                            .replace("{{mc}}", minecraftUser?.name ?: UNNAMED_ACCOUNT)
                    ).queue()
            }
        } catch (e: Exception) {
            // Just in case a DB interaction failed the user still needs to get a response.
            event.hook.setEphemeral(true)
                .sendMessageFormat(AstralBotTextConfig.LINK_ERROR.get().replace("{{error}}", e.localizedMessage))
                .queue()
            LOGGER.error("Failed to link", e)
        }

    }
}

/**
 * This command unlinks the issuing Discord user
 *
 * @author Erdragh
 */
object UnlinkCommand : HandledSlashCommand {
    private const val OPTION_USER = "user"
    override val command: SlashCommandData =
        Commands.slash("unlink", "Unlinks your Minecraft account with your Discord account")
            .addOption(OptionType.USER, OPTION_USER, "The user that will be unlinked. If not provided, the command issuer will be used.", false)

    override fun handle(event: SlashCommandInteractionEvent) {
        // DB Interactions could take a while, so the reply needs to get deferred
        event.deferReply(true).queue()
        val user = event.getOption(OPTION_USER)?.asUser

        when {
            user == null -> WhitelistHandler.unWhitelist(event.user)
            event.member?.hasPermission(Permission.MODERATE_MEMBERS) == true -> WhitelistHandler.unWhitelist(user)
            else -> {
                event.hook.setEphemeral(true).sendMessage(AstralBotTextConfig.UNLINK_NOPERMS.get()).queue()
                return
            }
        }
        event.hook.setEphemeral(true).sendMessageFormat(AstralBotTextConfig.UNLINK_UNLINKED.get().replace("{{name}}", (user ?: event.user).asMention)).queue()
    }
}


// Specifying option names as constants to prevent typos
private const val OPTION_MC = "mc"
private const val OPTION_DC = "dc"

/**
 * This command can check the link status of Discord Users and Minecraft accounts
 *
 * It has the following options:
 * - `mc`: a Minecraft username for which a possibly linked Discord User will be reported
 * - `dc`: a Discord user for which a possibly linked Minecraft account will be reported
 *
 * @author Erdragh
 */
object LinkCheckCommand : HandledSlashCommand, MinecraftUserAutocompleteCommand(OPTION_MC, OPTION_DC) {
    override val command = Commands.slash("linkcheck", "Checks link status of a specified Minecraft or Discord account")
        .addOption(OptionType.STRING, OPTION_MC, "Minecraft Username", false, true)
        .addOption(OptionType.MENTIONABLE, OPTION_DC, "Discord User", false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))

    /**
     * Handles the case the command issuer gave a Minecraft name
     * @param event the issued event for the command. Used to respond
     * @param minecraftName the Minecraft username the issuer provided
     */
    private fun handleMinecraftToDiscord(event: SlashCommandInteractionEvent, minecraftName: String) {
        // Since this message gets used in separate places the template is declared earlier
        val notFound = "Minecraft username %s is not linked to any Discord User"

        // Gets the associated [GameProfile] for the name to get the ID
        val minecraftProfile = minecraftHandler?.byName(minecraftName)

        // gets the discord ID
        val discordID = if (minecraftProfile != null) WhitelistHandler.checkWhitelist(minecraftProfile.id) else null
        if (discordID != null) {
            val guild = event.guild
            if (guild != null) {
                guild.retrieveMemberById(discordID).submit().whenComplete { member, error ->
                    if (error != null) {
                        event.hook.setEphemeral(true)
                            .sendMessageFormat(
                                "Something went wrong while trying to get link for %s: %s",
                                minecraftProfile?.name,
                                error.localizedMessage
                            )
                            .queue()
                    } else {
                        event.hook.setEphemeral(true)
                            .sendMessageFormat(
                                "Minecraft username %s is linked to %s",
                                minecraftProfile?.name ?: minecraftName,
                                member
                            ).queue()
                    }
                }
            } else {
                event.hook.setEphemeral(true).sendMessage("Something went wrong").queue()
            }
        } else {
            event.hook.setEphemeral(true).sendMessageFormat(
                notFound, minecraftProfile?.name ?: minecraftName
            ).queue()
        }
    }

    /**
     * Handles the case the command issuer gave a Discord user
     * @param event the issued event for the command. Used to respond
     * @param discordUser the Discord user the issuer provided
     */
    private fun handleDiscordToMinecraft(event: SlashCommandInteractionEvent, discordUser: Member) {
        val minecraftID = WhitelistHandler.checkWhitelist(discordUser.idLong)
        if (minecraftID != null) {
            val minecraftUser = minecraftHandler?.byUUID(minecraftID)
            if (minecraftUser != null) {
                event.hook.setEphemeral(true).sendMessageFormat(
                    "%s is linked to Minecraft username %s", discordUser.asMention, minecraftUser.name
                ).queue()
                return
            }
        }
        event.hook.setEphemeral(true).sendMessageFormat("%s not linked to any Minecraft username", discordUser.asMention).queue()
    }

    override fun handle(event: SlashCommandInteractionEvent) {
        // Since DB interactions and sending request to Discord may take a while
        // the response gets deferred
        event.deferReply(true).queue()

        val minecraftName = event.getOption(OPTION_MC)?.asString
        val discordUser = event.getOption(OPTION_DC)?.asMentionable

        when {
            minecraftName != null && discordUser != null -> {
                event.hook.setEphemeral(true)
                    .sendMessage("You need to specify either a Discord user or a Minecraft Username, not both").queue()
            }

            minecraftName != null -> {
                handleMinecraftToDiscord(event, minecraftName)
            }

            discordUser is Member -> {
                handleDiscordToMinecraft(event, discordUser)
            }

            else -> {
                event.hook.setEphemeral(true)
                    .sendMessage("You need to specify either a Discord user or a Minecraft Username").queue()
            }
        }
    }
}