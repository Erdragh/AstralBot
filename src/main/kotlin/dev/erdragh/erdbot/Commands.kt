package dev.erdragh.erdbot

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

val commands = arrayOf(
    EchoCommand,
    FaQCommand
)

interface HandledSlashCommand {
    val command: SlashCommandData
    fun handle(event: SlashCommandInteractionEvent)
}

interface AutocompleteCommand {
    fun autocomplete(event: CommandAutoCompleteInteractionEvent)
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
        val suggestions = FAQHandler.suggestFAQIds()
        if (suggestions != null && event.focusedOption.name == "id") {
            event.replyChoiceStrings(suggestions).queue()
        }
    }
}