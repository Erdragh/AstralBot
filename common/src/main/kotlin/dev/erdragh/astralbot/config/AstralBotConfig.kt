package dev.erdragh.astralbot.config

import net.minecraftforge.common.ForgeConfigSpec

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

        SPEC = builder.build()
    }
}