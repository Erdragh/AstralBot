package dev.erdragh.astralbot

import dev.erdragh.astralbot.commands.CommandHandlingListener
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOGGER: Logger = LoggerFactory.getLogger("AstralBot")
fun main() {
    val env = System.getenv()
    if (!env.containsKey("DISCORD_TOKEN")) {
        LOGGER.info("Not starting AstralBot because of missing DISCORD_TOKEN environment variable.")
        return
    }
    JDABuilder
        .createLight(env["DISCORD_TOKEN"],
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(CommandHandlingListener)
        .build().awaitReady()

    LOGGER.info("AstralBot fully started")
}