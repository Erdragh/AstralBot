package dev.erdragh.erdbot

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

val commands = arrayOf(
    RefreshCommandsCommand,
    EchoCommand,
    FaQCommand,
    LinkCommand,
    UnlinkCommand
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
            if (event.guild == null) event.hook.setEphemeral(true).sendMessage("Failed to fetch Guild to refresh").queue()
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
        val user = event.user

        event.reply("Linked user ${user.name}").setEphemeral(true).queue()
    }
}

object UnlinkCommand : HandledSlashCommand {
    override val command: SlashCommandData =
        Commands.slash("unlink", "Unlinks your Minecraft Account with your Discord Account")

    override fun handle(event: SlashCommandInteractionEvent) {
        val user = event.user

        event.reply("Unlinked user ${user.name}").setEphemeral(true).queue()
    }
}