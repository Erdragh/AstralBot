package dev.erdragh.astralbot.config

import dev.erdragh.astralbot.LOGGER
import net.minecraftforge.common.ForgeConfigSpec
import java.net.URL

/**
 * Config for the AstralBot mod. This uses Forge's config system
 * to reduce dependencies. This can be used on Fabric by using
 * the Forge Config API port.
 * @author Erdragh
 */
object AstralBotConfig {
    val SPEC: ForgeConfigSpec

    /**
     * Whether the default whitelisting process is respected or ignored.
     * Setting this to `true` will *force* every user who wants to join
     * the server to link their account, even Operators.
     */
    val REQUIRE_LINK_FOR_WHITELIST: ForgeConfigSpec.BooleanValue

    /**
     * The link to the Discord servers where users can run the /link command.
     * Gets used when formatting the message shown to users who try to log in
     * without being whitelisted.
     */
    val DISCORD_LINK: ForgeConfigSpec.ConfigValue<String>

    /**
     * The ID of the discord channel where the messages are synchronized
     */
    val DISCORD_CHANNEL: ForgeConfigSpec.ConfigValue<Long>

    /**
     * The ID of the Discord Guild (server) where this bot will be active.
     * This is used to get the chat sync channel etc.
     */
    val DISCORD_GUILD: ForgeConfigSpec.ConfigValue<Long>

    /**
     * The ID of the Discord role given to linked members
     */
    val DISCORD_ROLE: ForgeConfigSpec.ConfigValue<Long>

    /**
     * If this is set to true the message sent into the Minecraft chat
     * will be clickable and take you to the relevant message on Discord
     */
    val CLICKABLE_MESSAGES: ForgeConfigSpec.BooleanValue

    /**
     * If this is on the embeds and attached files on a message will be
     * handled and possibly displayed in messages sent to the players
     */
    val HANDLE_EMBEDS: ForgeConfigSpec.BooleanValue

    /**
     * If this is on embeds that have a URL associated with them will
     * be clickable.
     */
    val CLICKABLE_EMBEDS: ForgeConfigSpec.BooleanValue

    private val URL_BLOCKLIST: ForgeConfigSpec.ConfigValue<List<String>>

    init {
        val builder = ForgeConfigSpec.Builder()

        builder.comment("AstralBot Config")

        REQUIRE_LINK_FOR_WHITELIST = builder.comment("Whether to require being linked to be whitelisted")
            .define("requireLinkForWhitelist", false)
        DISCORD_LINK = builder.comment("Link to the discord where your users can run the /link command")
            .define("discordLink", "")
        DISCORD_CHANNEL = builder.comment("Channel ID where the chat messages are synced")
            .define("discordChannel", (-1).toLong())
        DISCORD_GUILD = builder.comment("Guild (server) ID where the chat messages etc. are synced")
            .define("discordGuild", (-1).toLong())
        DISCORD_ROLE = builder.comment("ID of the role given to linked users")
            .define("discordRole", (-1).toLong())

        CLICKABLE_MESSAGES =
            builder.comment("Whether to make messages sent into the Minecraft chat open the Discord chat when clicked")
                .define("clickableMessages", true)
        HANDLE_EMBEDS = builder.comment("Whether to display embeds and attached files on messages")
            .define("handleEmbeds", true)
        CLICKABLE_EMBEDS =
            builder.comment("Whether to add click events opening URLs that may be associated with embeds")
                .define("clickableEmbeds", true)

        URL_BLOCKLIST = builder.comment("URLs that don't get turned into clickable links")
            .defineList(
                "urlBlocklist",
                ArrayList(
                    listOf(
                        "https://pornhub.com",
                        "https://xhamster.com",
                        "https://xvideos.com",
                        "https://rule34.xyz"
                    )
                )
            ) {
                if (it !is String) {
                    LOGGER.warn("$it in URL blocklist is not a String")
                    return@defineList false
                }
                // TODO: Replace with better way to check for URL
                try {
                    URL(it)
                    return@defineList true
                } catch (e: Exception) {
                    LOGGER.warn("Failed to parse URL on blocklist: $it", e)
                    return@defineList false
                }
            }

        SPEC = builder.build()
    }

    /**
     * Decides if a given [url] is allowed based on the [URL_BLOCKLIST].
     *
     * @param url the URL to check
     * @return whether the URL is valid. A [url] of `null` is allowed.
     */
    fun urlAllowed(url: String?): Boolean {
        if (url == null) return true
        try {
            val parsedURL = URL(url)
            for (blockedURL in URL_BLOCKLIST.get()) {
                if (parsedURL.host.equals(URL(blockedURL).host)) return false
            }
        } catch (e: Exception) {
            LOGGER.warn("URL $url", e)
            return false
        }
        return true
    }
}