package dev.erdragh.astralbot

import dev.erdragh.astralbot.commands.CommandHandlingListener
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.handlers.FAQHandler
import dev.erdragh.astralbot.handlers.MinecraftHandler
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import kotlin.properties.Delegates

const val MODID = "astralbot"
val LOGGER: Logger = LoggerFactory.getLogger(MODID)

private lateinit var startTimestamp: LocalDateTime
var minecraftHandler: MinecraftHandler? = null

var textChannel: TextChannel? = null
var guild: Guild? = null
private var jda: JDA? = null
var applicationId by Delegates.notNull<Long>()

var baseDirectory: File? = null

private lateinit var setupJob: Job

/**
 * @return the time at which the AstralBot was started
 */
fun getStartTimestamp(): LocalDateTime {
    return startTimestamp
}

/**
 * Waits for the [setupJob] to be finished
 * before letting the caller continue.
 */
fun waitForSetup() = runBlocking {
    setupJob.join()
}

/**
 * Updates the Discord presence based on how many
 * players are online.
 * @param count how many players are online
 */
fun updatePresence(count: Int) {
    val message = when {
        count > 1 -> "$count players"
        count == 1 -> "$count player"
        else -> "an empty server"
    }
    jda?.presence?.setPresence(Activity.watching(message), count < 1)
}

/**
 * Handles all the setup that needs to be done when JDA
 * has finished connecting. This includes fetching the guild,
 * channel and role for things like Chat Synchronization, Linking
 * etc.
 */
private fun setupFromJDA(api: JDA) {
    api.awaitReady()
    LOGGER.info("Fetching required data from Discord")
    updatePresence(0)
    applicationId = api.retrieveApplicationInfo().submit().get().idLong
    if (AstralBotConfig.DISCORD_GUILD.get() < 0) {
        LOGGER.warn("No text channel for chat synchronization configured. Chat sync will not be enabled.")
        return
    }
    if (AstralBotConfig.DISCORD_CHANNEL.get() < 0) {
        LOGGER.warn("No text channel for chat synchronization configured. Chat sync will not be enabled.")
        return
    }
    val g = api.getGuildById(AstralBotConfig.DISCORD_GUILD.get())
    if (g == null) {
        LOGGER.warn("Configured Discord Guild (server) ID is not valid.")
        return
    }
    val ch = g.getTextChannelById(AstralBotConfig.DISCORD_CHANNEL.get())
    if (ch == null) {
        LOGGER.warn("Configured Discord channel ID is not valid.")
        return
    }
    textChannel = ch
    guild = g
}

@OptIn(DelicateCoroutinesApi::class)
fun startAstralbot(server: MinecraftServer) {
    startTimestamp = LocalDateTime.now()
    val env = System.getenv()

    baseDirectory = File(server.serverDirectory, MODID)
    if (baseDirectory!!.mkdir()) {
        LOGGER.debug("Created $MODID directory")
    }

    if (!env.containsKey("DISCORD_TOKEN")) {
        LOGGER.warn("Not starting AstralBot because of missing DISCORD_TOKEN environment variable.")
        return
    }

    minecraftHandler = MinecraftHandler(server)

    jda = JDABuilder.createLight(
            env["DISCORD_TOKEN"],
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS
        ).addEventListeners(CommandHandlingListener, minecraftHandler).build()

    setupJob = GlobalScope.async {
        launch {
            FAQHandler.start()
            setupFromJDA(jda!!)
            LOGGER.info("AstralBot started!")
        }
    }

    // This makes sure that the extra parallel tasks from this
    // mod/bot combo get shut down even if the Server Shutdown
    // Event never gets triggered.
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            stopAstralbot()
        }
    })
}

fun stopAstralbot() {
    LOGGER.info("Shutting down AstralBot")
    if (baseDirectory != null) FAQHandler.stop()
    jda?.shutdownNow()
    jda?.awaitShutdown()
    LOGGER.info("Shut down AstralBot")
}