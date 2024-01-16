package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.getStartTimestamp
import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
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

object StopCommand : HandledSlashCommand {
    override val command = Commands.slash("stop", "Stops the Minecraft server").setDefaultPermissions(
        DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
    )

    override fun handle(event: SlashCommandInteractionEvent) {
        event.interaction.reply("Stopping Minecraft Server").submit().whenComplete { _, _ ->
            minecraftHandler?.stopServer()
        }
    }
}

object TPSCommand : HandledSlashCommand {
    override val command = Commands.slash("tps", "Shows information about the tick speed").setDefaultPermissions(
        DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
    )

    override fun handle(event: SlashCommandInteractionEvent) {
        event.interaction.reply(minecraftHandler?.tickReport() ?: "No Information Available").queue()
    }
}