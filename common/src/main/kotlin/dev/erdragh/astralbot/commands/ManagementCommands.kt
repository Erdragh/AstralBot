package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.getStartTimestamp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.minecraft.util.Tuple
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object UptimeCommand : HandledSlashCommand {
    override val command = Commands.slash("uptime", "Prints the current uptime")

    override fun handle(event: SlashCommandInteractionEvent) {
        var start = getStartTimestamp()
        val now = LocalDateTime.now()
        val uptimeString = arrayOf(
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
        ).map {
            val inUnit = it.between(start, now)
            start = start.plus(inUnit, it)
            Tuple(inUnit, it)
        }.filter { it.a > 0 }.joinToString {
            "${it.a} ${it.b}"
        }

        event.interaction.reply("Uptime: $uptimeString")
            .queue()
    }
}