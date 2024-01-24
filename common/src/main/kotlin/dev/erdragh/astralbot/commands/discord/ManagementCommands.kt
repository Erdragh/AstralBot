package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.getStartTimestamp
import dev.erdragh.astralbot.minecraftHandler
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.log2
import kotlin.math.pow

/**
 * This command prints the current Uptime of the Minecraft Server,
 * measured from the time AstralBot was started (normally after world load).
 *
 * @author Erdragh
 */
object UptimeCommand : HandledSlashCommand {
    override val command = Commands.slash("uptime", "Prints the current uptime")

    override fun handle(event: SlashCommandInteractionEvent) {
        var start = getStartTimestamp()
        val now = LocalDateTime.now()

        // Dynamically format the uptime
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
            Pair(inUnit, it)
        }.filter { it.first > 0 }.joinToString { (amount, unit) ->
            "$amount $unit"
        }

        event.interaction.reply("Uptime: $uptimeString")
            .queue()
    }
}

/**
 * This command stops the Minecraft server the same way the
 * `/stop` command does from inside Minecraft
 *
 * @author Erdragh
 */
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

/**
 * Extension function taking from StackOverflow to format a Long
 * in Bytes and the associated SI Units
 */
private val Long.asFileSize: String
    get() = log2(coerceAtLeast(1).toDouble()).toInt().div(10).let {
        val precision = when (it) {
            0 -> 0; 1 -> 1; else -> 2
        }
        val prefix = arrayOf("", "K", "M", "G", "T", "P", "E", "Z", "Y")
        String.format("%.${precision}f ${prefix[it]}B", toDouble() / 2.0.pow(it * 10.0))
    }

/**
 * This command prints some Information about the ticking performance
 * of the Minecraft server
 *
 * @author Erdragh
 */
object TPSCommand : HandledSlashCommand {
    override val command = Commands.slash("tps", "Shows information about the tick speed")

    override fun handle(event: SlashCommandInteractionEvent) {
        event.interaction.reply(minecraftHandler?.tickReport() ?: "No Information Available").queue()
    }
}

/**
 * This command prints System usage information, e.g. Memory and CPU
 *
 * @author Erdragh
 */
object UsageCommand : HandledSlashCommand {
    override val command = Commands.slash("usage", "Shows information about the hardware usage")

    // Java interfaces for gathering the Data
    private val memoryBean = ManagementFactory.getMemoryMXBean()
    private val runtime = Runtime.getRuntime()

    // Format for the CPU Usage numbers
    private val numberFormat = DecimalFormat("###.##")

    // This is a var because it will be dynamically set based on what
    // classes are available in the environment
    private var getCPUUsage: () -> Pair<Double, Double>

    init {
        // The com.sun.management classes aren't available on certain versions of Java,
        // so if it's not available getCPUUsage needs to be dynamically set to prevent
        // loading of unavailable classes
        try {
            Class.forName("com.sun.management.OperatingSystemMXBean")
            getCPUUsage = ManagementHelper::getCpuUsage
        } catch (e: ClassNotFoundException) {
            LOGGER.warn("com.sun.management.OperatingSystemMXBean class not available, Usage information will be limited.", e)
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