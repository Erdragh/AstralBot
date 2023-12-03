package dev.erdragh.astralbot

import dev.erdragh.astralbot.commands.CommandHandlingListener
import dev.erdragh.astralbot.handlers.FAQHandler
import dev.erdragh.astralbot.handlers.MinecraftHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

const val MODID = "astralbot"
val LOGGER: Logger = LoggerFactory.getLogger(MODID)
var minecraftHandler: MinecraftHandler? = null
var jda: JDA? = null
lateinit var baseDirectory: File

fun startAstralbot(server: MinecraftServer) {
    val env = System.getenv()
    if (!env.containsKey("DISCORD_TOKEN")) {
        LOGGER.warn("Not starting AstralBot because of missing DISCORD_TOKEN environment variable.")
        return
    }

    baseDirectory = File(server.serverDirectory, MODID)
    if (baseDirectory.mkdir()) {
        LOGGER.debug("Created $MODID directory")
    }

    FAQHandler.start()

    jda = JDABuilder.createLight(
            env["DISCORD_TOKEN"],
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS
        ).addEventListeners(CommandHandlingListener).build()

    minecraftHandler = MinecraftHandler(server, jda)

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
    FAQHandler.stop()
    jda?.shutdownNow()
    jda?.awaitShutdown()
    LOGGER.info("Shut down AstralBot")
}