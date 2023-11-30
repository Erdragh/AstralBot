package dev.erdragh.astralbot

import dev.erdragh.astralbot.commands.CommandHandlingListener
import dev.erdragh.astralbot.handlers.MinecraftHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOGGER: Logger = LoggerFactory.getLogger("AstralBot")
var minecraftHandler: MinecraftHandler? = null
fun setupAstralbot(server: MinecraftServer) {
    minecraftHandler = MinecraftHandler(server)
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