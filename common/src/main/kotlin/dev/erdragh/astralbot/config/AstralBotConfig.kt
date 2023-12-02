package dev.erdragh.astralbot.config

import net.minecraftforge.common.ForgeConfigSpec

object AstralBotConfig {
    val SPEC: ForgeConfigSpec

    val REQUIRE_LINK_FOR_WHITELIST: ForgeConfigSpec.BooleanValue

    init {
        val builder = ForgeConfigSpec.Builder()

        builder.comment("AstralBot Config")

        REQUIRE_LINK_FOR_WHITELIST = builder.comment("Whether to require being linked to be whitelisted")
            .define("requireLinkForWhitelist", false)

        SPEC = builder.build()
    }
}