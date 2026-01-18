package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * This command lists all currently online players
 *
 * @author Erdragh
 */
object ListCommand : HandledSlashCommand {
    override val command: SlashCommandData = Commands.slash("list", "Lists currently online players")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        val list = minecraftHandler?.getOnlinePlayers()?.map { "- $it" }

        if (!list.isNullOrEmpty()) {
            event.hook.sendMessage("The following players are currently online:\n${list.joinToString("\n")}").queue()
        } else {
            event.hook.sendMessage("There are no players online currently").queue()
        }
    }
}