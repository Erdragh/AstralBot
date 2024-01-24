package dev.erdragh.astralbot.config

import net.minecraftforge.common.ForgeConfigSpec

object AstralBotTextConfig {
    val SPEC: ForgeConfigSpec

    val FAQ_ERROR: ForgeConfigSpec.ConfigValue<String>
    val FAQ_NO_REGISTERED: ForgeConfigSpec.ConfigValue<String>

    init {
        val builder = ForgeConfigSpec.Builder()

        FAQ_ERROR = builder.comment("Message sent to Discord if an error ocurrs during FAQ loading")
            .define("faqError", "Bot Error (Contact Bot Operator)")
        FAQ_NO_REGISTERED =
            builder.comment(
                """Message sent to Discord when there is no FAQ for the given id.
                The placeholder {{id}} may be used to include the requested id""".trimMargin()
            )
                .define("faqNoRegistered", "No FAQ registered for id: `{{id}}`")

        SPEC = builder.build()
    }
}