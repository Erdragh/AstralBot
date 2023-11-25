package dev.erdragh.erdbot.commands

import dev.erdragh.erdbot.handlers.WhitelistHandler
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.util.*

object LinkCommand : HandledSlashCommand {
    private const val OPTION_CODE = "code"

    override val command: SlashCommandData =
        Commands.slash("link", "Links your Minecraft Account with your Discord Account")
            .addOption(OptionType.NUMBER, OPTION_CODE, "your personal link code", true)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        val linkCode = event.getOption(OPTION_CODE)?.asString
        val minecraftUser =
            UUID.fromString("decdaa2b-c56e-49e8-862d-bbdd89a15b0a") // TODO: Get actual user ID via minecraft server

        try {
            WhitelistHandler.whitelist(event.user, minecraftUser)
            event.hook.setEphemeral(true)
                .sendMessageFormat("Linked %s to MC: %s", event.member, minecraftUser).queue()
        } catch (e: Exception) {
            event.hook.setEphemeral(true).sendMessageFormat("Failed to link: %s", e.localizedMessage).queue()
        }

    }
}

object UnlinkCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("unlink", "Unlinks your Minecraft Account with your Discord Account")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        WhitelistHandler.unWhitelist(event.user)

        event.hook.setEphemeral(true).sendMessageFormat("Unlinked %s", event.user).queue()
    }
}

object LinkCheckCommand : HandledSlashCommand, AutocompleteCommand {
    private const val OPTION_MC = "mc"
    private const val OPTION_DC = "dc"

    override val command = Commands.slash("linkcheck", "Checks link status of a specified Minecraft or Discord Account")
        .addOption(OptionType.STRING, OPTION_MC, "Minecraft Username", false, true)
        .addOption(OptionType.MENTIONABLE, OPTION_DC, "Discord User", false)

    private fun handleMinecraftToDiscord(event: SlashCommandInteractionEvent, minecraftName: String) {
        val discordID =
            WhitelistHandler.checkWhitelist(UUID.fromString("decdaa2b-c56e-49e8-862d-bbdd89a15b0a")) // TODO: Get actual minecraft user from server
        if (discordID != null) {
            val guild = event.guild
            if (guild != null) {
                var found = false
                guild.loadMembers {
                    if (it.idLong == discordID) {
                        event.hook.setEphemeral(true)
                            .sendMessageFormat("Minecraft username %s is linked to %s", minecraftName, it)
                            .queue()
                        found = true
                    }
                }.onError {
                    event.hook.setEphemeral(true)
                        .sendMessageFormat("Something went wrong: %s", it.localizedMessage)
                        .queue()
                }.onSuccess {
                    if (!found) {
                        event.hook.setEphemeral(true)
                            .sendMessageFormat(
                                "Minecraft username %s is not linked to any Discord User",
                                minecraftName
                            ).queue()
                    }
                }
            } else {
                event.hook.setEphemeral(true)
                    .sendMessage("Something went wrong")
                    .queue()
            }
        }
    }

    private fun handleDiscordToMinecraft(event: SlashCommandInteractionEvent, discordUser: Member) {
        val minecraftID = WhitelistHandler.checkWhitelist(discordUser.idLong)
        if (minecraftID != null) {
            val minecraftUser = "Erdragh" // TODO: Get using actual minecraftID from server instance
            if (minecraftUser != null) {
                event.hook.setEphemeral(true).sendMessageFormat(
                    "%s is linked to Minecraft username %s",
                    discordUser,
                    minecraftUser
                ).queue()
                return
            }
        }
        event.hook.setEphemeral(true)
            .sendMessageFormat("%s not linked to any Minecraft username", discordUser).queue()
    }

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()

        val minecraftName = event.getOption(OPTION_MC)?.asString
        val discordUser = event.getOption(OPTION_DC)?.asMentionable

        if (minecraftName == null && discordUser == null) {
            event.hook.setEphemeral(true)
                .sendMessage("You need to specify either a Discord user or a Minecraft Username").queue()
        } else if (minecraftName != null && discordUser != null) {
            event.hook.setEphemeral(true)
                .sendMessage("You need to specify either a Discord user or a Minecraft Username, not both").queue()
        } else if (minecraftName != null) {
            handleMinecraftToDiscord(event, minecraftName)
        } else if (discordUser is Member) {
            handleDiscordToMinecraft(event, discordUser)
        } else {
            event.hook.setEphemeral(true)
                .sendMessage("You need to specify either a Discord user or a Minecraft Username").queue()
        }
    }

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == OPTION_MC) {
            // TODO: Suggest currently online users
            event.replyChoiceStrings(arrayOf("Erdragh").filter { it.startsWith(event.focusedOption.value) }).queue()
        }
    }
}