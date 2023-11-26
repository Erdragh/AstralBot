package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.handlers.FAQHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

val commands = arrayOf(
    RefreshCommandsCommand,
    FAQCommand,
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

object FAQCommand : HandledSlashCommand, AutocompleteCommand {
    private const val OPTION_ID = "id"

    override val command: SlashCommandData = Commands.slash("faq", "prints a specified FAQ answer")
        .addOption(OptionType.STRING, OPTION_ID, "id of the faq", true, true)

    override fun handle(event: SlashCommandInteractionEvent) {
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
