package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.handlers.FAQHandler
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import kotlin.math.min

/**
 * This command prints FAQ messages, fetching them from
 * disk using the [FAQHandler].
 *
 * It has the following options:
 * - `id`: used to fetch the FAQ from disk
 *
 * @author Erdragh
 */
object FAQCommands : HandledSlashCommand, AutocompleteCommand {
    private const val OPTION_ID = "id"

    override val command: SlashCommandData = Commands.slash("faq", "Prints a specified FAQ answer")
        .addOption(OptionType.STRING, OPTION_ID, "ID of the FAQ", true, true)

    override fun handle(event: SlashCommandInteractionEvent) {
        // since interacting with the disk may take a while, the reply gets deferred
        event.deferReply(false).queue()

        val faqId = event.getOption(OPTION_ID)!!.asString
        val faqResponse = FAQHandler.getFAQForId(faqId)

        if (faqResponse.length >= Message.MAX_CONTENT_LENGTH) {
            event.hook.sendMessage(AstralBotTextConfig.FAQ_TOO_LONG.get().replace("{{id}}", faqId)).queue()
        } else {
            event.hook.sendMessage(faqResponse).queue()
        }

    }

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == OPTION_ID) {
            val faqs = FAQHandler.suggestFAQIds(event.focusedOption.value)
            event.replyChoiceStrings(faqs.slice(0..<min(OptionData.MAX_CHOICES, faqs.size))).queue()
        }
    }
}

object ListFaqsCommand : HandledSlashCommand, AutocompleteCommand {
    private const val OPTION_SLUG = "slug"
    override val command: SlashCommandData = Commands.slash("listfaq", "Lists available FAQs")
        .addOption(OptionType.STRING, OPTION_SLUG, "Optional search key, narrows the results to only those that include the given input", false, true)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        val faqId = event.getOption(OPTION_SLUG)?.asString

        val faqs = FAQHandler.suggestFAQIds(faqId)

        val msg = if (faqs.isEmpty()) AstralBotTextConfig.FAQ_NONE_AVAILABLE.get() else faqs.joinToString(
            "\n- ",
            "- "
        ) { "`$it`" }

        event.hook.sendMessage(msg).queue()
    }

    override fun autocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == OPTION_SLUG) {
            val faqs = FAQHandler.suggestFAQIds(event.focusedOption.value)
            event.replyChoiceStrings(faqs.slice(0..<min(OptionData.MAX_CHOICES, faqs.size))).queue()
        }
    }
}