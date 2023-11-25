package dev.erdragh.erdbot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOGGER: Logger = LoggerFactory.getLogger("ErdBot")
fun main() {
    val env = System.getenv()
    if (!env.containsKey("DISCORD_TOKEN")) throw IllegalStateException("No DISCORD_TOKEN in environment")
    val api = JDABuilder
        .createLight(env["DISCORD_TOKEN"],
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(CommandHandlingListener)
        .build().awaitReady()

    LOGGER.info("ErdBot fully started")
}