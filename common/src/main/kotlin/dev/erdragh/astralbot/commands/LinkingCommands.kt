package dev.erdragh.astralbot.commands

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
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
            event.hook.setEphemeral(true).sendMessage("Couldn't find Minecraft account to link").queue()
            return
        }

        // Gets the user profile of the given Minecraft user ID to get access to the name
        val minecraftUser = minecraftHandler?.byUUID(minecraftID)

        try {
            // Depending on the whitelisting status of the given data send the relevant response
            if (WhitelistHandler.checkWhitelist(minecraftID) != null) {
                event.hook.setEphemeral(true)
                    .sendMessageFormat("Minecraft username %s already linked", minecraftUser?.name)
                    .queue()
            } else if (WhitelistHandler.checkWhitelist(event.user.idLong) != null) {
                event.hook.setEphemeral(true).sendMessageFormat("%s already linked", event.member).queue()
            } else {
                WhitelistHandler.whitelist(event.user, minecraftID)
                event.hook.setEphemeral(true)
                    .sendMessageFormat("Linked %s to Minecraft username %s", event.member, minecraftUser?.name).queue()
            }
        } catch (e: Exception) {
            // Just in case a DB interaction failed the user still needs to get a response.
            event.hook.setEphemeral(true).sendMessageFormat("Failed to link: %s", e.localizedMessage).queue()
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
    override val command: SlashCommandData =
        Commands.slash("unlink", "Unlinks your Minecraft account with your Discord account")

    override fun handle(event: SlashCommandInteractionEvent) {
        // DB Interactions could take a while, so the reply needs to get deferred
        event.deferReply(true).queue()

        WhitelistHandler.unWhitelist(event.user)

        event.hook.setEphemeral(true).sendMessageFormat("Unlinked %s", event.user).queue()
    }
}

/**
 * This command can check the link status of Discord Users and Minecraft accounts
 *
 * It has the following options:
 * - `mc`: a Minecraft username for which a possibly linked Discord User will be reported
 * - `dc`: a Discord user for which a possibly linked Minecraft account will be reported
 *
 * @author Erdragh
 */
object LinkCheckCommand : HandledSlashCommand, AutocompleteCommand {
    // Specifying option names as constants to prevent typos
    private const val OPTION_MC = "mc"
    private const val OPTION_DC = "dc"

    override val command = Commands.slash("linkcheck", "Checks link status of a specified Minecraft or Discord account")
        .addOption(OptionType.STRING, OPTION_MC, "Minecraft Username", false, true)
        .addOption(OptionType.MENTIONABLE, OPTION_DC, "Discord User", false)

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
                // TODO: Replace with more performant solution
                var found = false
                guild.loadMembers {
                    if (it.idLong == discordID) {
                        event.hook.setEphemeral(true)
                            .sendMessageFormat(
                                "Minecraft username %s is linked to %s",
                                minecraftProfile?.name ?: minecraftName,
                                it
                            ).queue()
                        found = true
                    }
                }.onError {
                    event.hook.setEphemeral(true).sendMessageFormat("Something went wrong: %s", it.localizedMessage)
                        .queue()
                }.onSuccess {
                    if (!found) {
                        event.hook.setEphemeral(true).sendMessageFormat(
                            notFound, minecraftProfile?.name ?: minecraftName
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
                    "%s is linked to Minecraft username %s", discordUser, minecraftUser.name
                ).queue()
                return
            }
        }
        event.hook.setEphemeral(true).sendMessageFormat("%s not linked to any Minecraft username", discordUser).queue()
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

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == OPTION_MC) {
            // TODO: Maybe cache this for servers with a lot of members?
            val minecraftUsers = minecraftHandler?.getOnlinePlayers()?.map(GameProfile::getName)
            event.replyChoiceStrings(minecraftUsers?.filter { it.startsWith(event.focusedOption.value) } ?: listOf())
                .queue()
        }
    }
}