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
    minecraftHandler = MinecraftHandler(server)
    baseDirectory = File(server.serverDirectory, MODID)
    baseDirectory.mkdir()
    FAQHandler.start()
    val env = System.getenv()
    if (!env.containsKey("DISCORD_TOKEN")) {
        LOGGER.info("Not starting AstralBot because of missing DISCORD_TOKEN environment variable.")
        return
    }
    jda = JDABuilder
        .createLight(
            env["DISCORD_TOKEN"],
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS
        )
        .addEventListeners(CommandHandlingListener)
        .build()

    LOGGER.info("AstralBot fully started")

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            stopAstralbot()
        }
    })
}

fun stopAstralbot() {
    LOGGER.info("Shutting down AstralBot")
    LOGGER.info("JDA: {}", jda)
    FAQHandler.stop()
    LOGGER.info("Shutting JDA")
    jda?.shutdownNow()
    LOGGER.info("After shutdownNow")
    jda?.awaitShutdown()
    LOGGER.info("Shut down AstralBot")
}