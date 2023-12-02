package dev.erdragh.astralbot.commands

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.handlers.MinecraftHandler
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.minecraftHandler
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
        val linkCodeInt = linkCode?.toDoubleOrNull()?.toInt()
        val minecraftID = if (linkCodeInt != null) WhitelistHandler.getPlayerFromCode(linkCodeInt) else null

        if (minecraftID == null) {
            event.hook.setEphemeral(true).sendMessage("Couldn't find minecraft account to link").queue()
            return
        }

        val minecraftUser = minecraftHandler?.byUUID(minecraftID)

        try {
            if (WhitelistHandler.checkWhitelist(minecraftID) != null) {
                event.hook.setEphemeral(true).sendMessageFormat("Minecraft username %s already linked", minecraftUser?.name)
                    .queue()
            } else if (WhitelistHandler.checkWhitelist(event.user.idLong) != null) {
                event.hook.setEphemeral(true).sendMessageFormat("%s already linked", event.member).queue()
            } else {
                WhitelistHandler.whitelist(event.user, minecraftID)
                event.hook.setEphemeral(true)
                    .sendMessageFormat("Linked %s to Minecraft username %s", event.member, minecraftUser?.name).queue()
            }
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
        val notFound = "Minecraft username %s is not linked to any Discord User"

        val minecraftProfile = minecraftHandler?.byName(minecraftName)

        val discordID = if (minecraftProfile != null) WhitelistHandler.checkWhitelist(minecraftProfile.id) else null
        if (discordID != null) {
            val guild = event.guild
            if (guild != null) {
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
            val minecraftUsers = minecraftHandler?.getOnlinePlayers()?.map(GameProfile::getName)
            event.replyChoiceStrings(minecraftUsers?.filter { it.startsWith(event.focusedOption.value) } ?: listOf())
                .queue()
        }
    }
}