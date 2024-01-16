package dev.erdragh.astralbot.commands

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.getStartTimestamp
import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.minecraft.util.Tuple
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.log2
import kotlin.math.pow

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

private val Long.asFileSize: String
    get() = log2(coerceAtLeast(1).toDouble()).toInt().div(10).let {
        val precision = when (it) {
            0 -> 0; 1 -> 1; else -> 2
        }
        val prefix = arrayOf("", "K", "M", "G", "T", "P", "E", "Z", "Y")
        String.format("%.${precision}f ${prefix[it]}B", toDouble() / 2.0.pow(it * 10.0))
    }

object TPSCommand : HandledSlashCommand {
    override val command = Commands.slash("tps", "Shows information about the tick speed")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.interaction.reply(minecraftHandler?.tickReport() ?: "No Information Available").queue()
    }
}

object UsageCommand : HandledSlashCommand {
    override val command = Commands.slash("usage", "Shows information about the hardware usage")

    private val memoryBean = ManagementFactory.getMemoryMXBean()
    private val runtime = Runtime.getRuntime()

    private val numberFormat = DecimalFormat("###.##")

    private var getCPUUsage: () -> Pair<Double, Double>

    init {
        try {
            Class.forName("com.sun.management.OperatingSystemMXBean")
            getCPUUsage = ManagementHelper::getCpuUsage
        } catch (e: ClassNotFoundException) {
            LOGGER.warn("com.sun.management.OperatingSystemMXBean class not available, Usage information will be limited.", e)
            getCPUUsage = { Pair(-1.0, -1.0) }
        } catch (e: Exception) {
            LOGGER.warn("com.sun.management.OperatingSystemMXBean::getProcessCpuLoad method not available, Usage information will be limited.", e)
            getCPUUsage = { Pair(-1.0, -1.0) }
        }
    }

    override fun handle(event: SlashCommandInteractionEvent) {
        val heapUsage = memoryBean.heapMemoryUsage.used
        val nonHeapUsage = memoryBean.nonHeapMemoryUsage.used

        val (processUsage, systemUsage) = getCPUUsage()

        event.interaction.reply(
            """
            CPU Usage:
            ```
            Process:  ${if (processUsage >= 0) "${numberFormat.format(processUsage * 100)}%" else "N/A"}
            System:   ${if (systemUsage >= 0) "${numberFormat.format(systemUsage * 100)}%" else "N/A"}
            ```
            Memory Usage:
            ```
            Heap:     ${heapUsage.asFileSize}
            Non Heap: ${nonHeapUsage.asFileSize}
            Total:    ${(heapUsage + nonHeapUsage).asFileSize}/${runtime.totalMemory().asFileSize}
            ```
        """.trimIndent()
        ).queue()

    }
}