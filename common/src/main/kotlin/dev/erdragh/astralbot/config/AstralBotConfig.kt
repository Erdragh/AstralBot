package dev.erdragh.astralbot.config

import net.minecraftforge.common.ForgeConfigSpec

object AstralBotConfig {
    val SPEC: ForgeConfigSpec

    val REQUIRE_LINK_FOR_WHITELIST: ForgeConfigSpec.BooleanValue
    val DISCORD_LINK: ForgeConfigSpec.ConfigValue<String>

    init {
        val builder = ForgeConfigSpec.Builder()

        builder.comment("AstralBot Config")

        REQUIRE_LINK_FOR_WHITELIST = builder.comment("Whether to require being linked to be whitelisted")
            .define("requireLinkForWhitelist", false)
        DISCORD_LINK = builder.comment("Link to the discord where your users can run the /link command")
            .define("discordLink", "")

        SPEC = builder.build()
    }
}