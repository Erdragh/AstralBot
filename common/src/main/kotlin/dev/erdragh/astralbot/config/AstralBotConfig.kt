package dev.erdragh.astralbot.config

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.commands.discord.allCommands
import net.minecraftforge.common.ForgeConfigSpec
import java.net.URI

/**
 * Config for the AstralBot mod. This uses Forge's config system
 * to reduce dependencies. This can be used on Fabric by using
 * the Forge Config API port.
 * @author Erdragh
 */
object AstralBotConfig {
    val SPEC: ForgeConfigSpec

    /**
     * If the `DISCORD_TOKEN` environment variable isn't set, the bot will
     * look for a token in this config option.
     */
    val DISCORD_TOKEN: ForgeConfigSpec.ConfigValue<String>

    /**
     * Used for sending more appealing messages
     */
    val WEBHOOK_URL: ForgeConfigSpec.ConfigValue<String>
    /**
     * Whether the configured [WEBHOOK_URL] should actually be used,
     * useful in case somebody wants to temporarily disable using
     * the webhook without removing the URL
     */
    val WEBHOOK_ENABLED: ForgeConfigSpec.BooleanValue

    /**
     * URL template for getting avatars from Minecraft users
     */
    val WEBHOOK_MC_AVATAR_URL: ForgeConfigSpec.ConfigValue<String>

    /**
     * Whether the chat messages sent via the webhook should
     * imitate the sender's Discord account or their Minecraft
     * account. If this is on, the linked Discord account will
     * be used.
     */
    val WEBHOOK_USE_LINKED: ForgeConfigSpec.BooleanValue

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
    val DISCORD_CHANNEL: ForgeConfigSpec.LongValue

    /**
     * The ID of the Discord Guild (server) where this bot will be active.
     * This is used to get the chat sync channel etc.
     */
    val DISCORD_GUILD: ForgeConfigSpec.LongValue

    /**
     * The ID of the Discord role given to linked members
     */
    val DISCORD_ROLE: ForgeConfigSpec.LongValue

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

    /**
     * List of Strings containing the URLs that are blocked by default.
     * If you read this and want to add more to the default list, feel
     * free to open an Issue or Pull Request.
     */
    private val URL_BLOCKLIST: ForgeConfigSpec.ConfigValue<List<String>>

    /**
     * List of the names of the commands that are enabled by default.
     * On first startup of a server, it will be all available commands.
     */
    val ENABLED_COMMANDS: ForgeConfigSpec.ConfigValue<List<String>>

    /**
     * Enables parsing Discord messages into Minecraft's Chat Components.
     * This includes making links clickable, etc.
     */
    val ENABLE_MARKDOWN_PARSING: ForgeConfigSpec.BooleanValue

    /**
     * Enables converting detected URLs into clickable links, requires
     * [ENABLE_MARKDOWN_PARSING] to be enabled to do anything
     */
    val ENABLE_AUTO_LINKS: ForgeConfigSpec.BooleanValue

    init {
        val builder = ForgeConfigSpec.Builder()

        builder.comment("AstralBot Config")

        DISCORD_TOKEN = builder.comment("Discord token for the bot. Can also be supplied via DISCORD_TOKEN environment variable")
            .define("token", "")

        WEBHOOK_URL = builder.comment("URL to the webhook where the messages will be sent from")
            .define(listOf("webhook", "url"), "")
        WEBHOOK_ENABLED = builder.comment("Whether to use the configured webhook for sending messages")
            .define(listOf("webhook", "enabled"), true)
        WEBHOOK_USE_LINKED = builder.comment("Whether to imitate user's linked Discord accounts when sending messages from MC to DC")
            .define(listOf("webhook", "useLinked"), false)
        WEBHOOK_MC_AVATAR_URL = builder.comment("API that returns images based on Minecraft users. {{uuid}} and {{name}} can be used")
            .define(listOf("webhook", "mcAvatarUrl"), "https://mc-heads.net/head/{{uuid}}")

        REQUIRE_LINK_FOR_WHITELIST = builder.comment("Whether to require being linked to be whitelisted")
            .define("requireLinkForWhitelist", false)
        DISCORD_LINK = builder.comment("Link to the discord where your users can run the /link command")
            .define("discordLink", "")
        DISCORD_CHANNEL = builder.comment("Channel ID where the chat messages are synced")
            .defineInRange("discordChannel", 0L, 0L, Long.MAX_VALUE)
        DISCORD_GUILD = builder.comment("Guild (server) ID where the chat messages etc. are synced")
            .defineInRange("discordChannel", 0L, 0L, Long.MAX_VALUE)
        DISCORD_ROLE = builder.comment("ID of the role given to linked users")
            .defineInRange("discordChannel", 0L, 0L, Long.MAX_VALUE)

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
                        "https://rule34.xyz",
                        "https://rule34.xxx",
                        "https://discord.gg"
                    )
                )
            ) {
                if (it !is String) {
                    LOGGER.warn("$it in URI blocklist is not a String")
                    return@defineList false
                }
                // TODO: Replace with better way to check for URL
                try {
                    URI(it)
                    return@defineList true
                } catch (e: Exception) {
                    LOGGER.warn("Failed to parse URI on blocklist: $it", e)
                    return@defineList false
                }
            }

        ENABLED_COMMANDS = builder.comment("Enabled Slash Commands")
            .defineList("enabledCommands",
                allCommands.map { it.command.name },
            ) {
                if (it !is String) {
                    LOGGER.warn("$it in enabledCommands is not a String")
                    return@defineList false
                }
                if (!allCommands.map { it.command.name }.contains(it)) {
                    LOGGER.warn("$it in enabledCommands doesn't exist")
                    return@defineList false
                }
                return@defineList true
            }

        ENABLE_MARKDOWN_PARSING = builder.comment("Parse Discord messages into Minecraft's Chat Components")
            .define(listOf("markdown", "enabled"), true)
        ENABLE_AUTO_LINKS = builder.comment("Automatically convert detected URLs into clickable links")
            .define(listOf("markdown", "autoLinks"), true)

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
            val parsedURL = URI(url)
            for (blockedURL in URL_BLOCKLIST.get()) {
                if (parsedURL.host == URI(blockedURL).host) return false
            }
        } catch (e: Exception) {
            LOGGER.warn("URI $url", e)
            return false
        }
        return true
    }
}