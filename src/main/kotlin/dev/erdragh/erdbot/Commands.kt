package dev.erdragh.erdbot

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.util.*

val commands = arrayOf(
    RefreshCommandsCommand,
    EchoCommand,
    FaQCommand,
    LinkCommand,
    UnlinkCommand,
    LinkCheckCommand
)

interface HandledSlashCommand {
    val command: SlashCommandData
    fun handle(event: SlashCommandInteractionEvent)
}

interface AutocompleteCommand {
    fun autocomplete(event: CommandAutoCompleteInteractionEvent)
}

object RefreshCommandsCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("reload", "Reloads the Discord Bot integrations (commands, etc.)")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        event.guild?.updateCommands()?.addCommands(commands.map { it.command })?.queue {
            if (event.guild == null) event.hook.setEphemeral(true).sendMessage("Failed to fetch Guild to refresh")
                .queue()
            else event.hook.setEphemeral(true).sendMessage("Reloaded commands for guild").queue()
        }
    }
}

object EchoCommand : HandledSlashCommand {
    override val command: SlashCommandData = Commands.slash("echo", "Repeats a Message")
        .addOption(OptionType.STRING, "message", "the message to repeat", true)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.reply("Echo Echo echo echo ... ${event.getOption("message")!!.asString}").queue()
    }
}

object FaQCommand : HandledSlashCommand, AutocompleteCommand {
    override val command: SlashCommandData = Commands.slash("faq", "prints a specified FAQ answer")
        .addOption(OptionType.STRING, "id", "id of the faq", true, true)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        val faqId = event.getOption("id")!!.asString
        val faqResponse = FAQHandler.getFAQForId(faqId)

        event.hook.sendMessage(faqResponse).queue()
    }

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == "id") {
            event.replyChoiceStrings(FAQHandler.suggestFAQIds(event.focusedOption.value)).queue()
        }
    }
}

object LinkCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("link", "Links your Minecraft Account with your Discord Account")
            .addOption(OptionType.NUMBER, "code", "your personal link code", true)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        val linkCode = event.getOption("code")?.asString
        val minecraftUser = UUID.fromString("decdaa2b-c56e-49e8-862d-bbdd89a15b0a") // TODO: Get actual user ID via minecraft server

        WhitelistHandler.whitelist(event.user, minecraftUser)

        event.hook.setEphemeral(true).sendMessageFormat("Linked Discord user {} to Minecraft username {}", minecraftUser).queue()
    }
}

object UnlinkCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("unlink", "Unlinks your Minecraft Account with your Discord Account")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        WhitelistHandler.unWhitelist(event.user)

        event.hook.setEphemeral(true).sendMessageFormat("Unlinked Discord user {}", event.user).queue()
    }
}

object LinkCheckCommand : HandledSlashCommand, AutocompleteCommand {
    override val command = Commands.slash("linkcheck", "Checks link status of a specified Minecraft or Discord Account")
        .addOption(OptionType.STRING, "mcname", "Minecraft Username", false, true)
        .addOption(OptionType.MENTIONABLE, "discorduser", "Discord User", false)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()

        val minecraftName = event.getOption("mcname")?.asString
        val discordUser = event.getOption("discorduser")?.asMentionable

        if (minecraftName == null && discordUser == null) {
            event.hook.setEphemeral(true)
                .sendMessage("You need to specify either a Discord user or a Minecraft Username").queue()
        } else if (minecraftName != null && discordUser != null) {
            event.hook.setEphemeral(true)
                .sendMessage("You need to specify either a Discord user or a Minecraft Username, not both").queue()
        } else if (minecraftName != null) {
            val discordID = WhitelistHandler.checkWhitelist(UUID.fromString("decdaa2b-c56e-49e8-862d-bbdd89a15b0a")) // TODO: Get actual minecraft user from server
            if (discordID != null) {
                val user = event.guild?.getMember(UserSnowflake.fromId(discordID))
                if (user != null) {
                    event.hook.setEphemeral(true)
                        .sendMessageFormat("Minecraft username {} linked to Discord user {}", minecraftName, user)
                        .queue()
                    return
                }
                event.hook.setEphemeral(true)
                    .sendMessageFormat("Minecraft username {} not linked to any Discord User", minecraftName).queue()
            }
        } else if (discordUser is User) {
            val minecraftID = WhitelistHandler.checkWhitelist(discordUser.idLong)
            if (minecraftID != null) {
                val minecraftUser = "Erdragh" // TODO: Get using actual minecraftID from server instance
                if (minecraftUser != null) {
                    event.hook.setEphemeral(true).sendMessageFormat(
                        "Discord user {} linked to Minecraft username {}",
                        discordUser,
                        minecraftUser
                    ).queue()
                    return
                }
                event.hook.setEphemeral(true)
                    .sendMessageFormat("Discord username {} not linked to any Minecraft username", discordUser).queue()
            }
        } else {
            event.hook.setEphemeral(true)
                .sendMessage("You need to specify either a Discord user or a Minecraft Username").queue()
        }
    }

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        TODO("Not yet implemented")
    }
}