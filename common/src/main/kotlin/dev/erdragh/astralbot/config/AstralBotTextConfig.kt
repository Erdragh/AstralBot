package dev.erdragh.astralbot.config

import net.minecraftforge.common.ForgeConfigSpec

object AstralBotTextConfig {
    val SPEC: ForgeConfigSpec

    val GENERIC_ERROR: ForgeConfigSpec.ConfigValue<String>

    val FAQ_ERROR: ForgeConfigSpec.ConfigValue<String>
    val FAQ_NO_REGISTERED: ForgeConfigSpec.ConfigValue<String>

    val TICK_REPORT: ForgeConfigSpec.ConfigValue<String>

    val PLAYER_MESSAGE: ForgeConfigSpec.ConfigValue<String>

    val DISCORD_MESSAGE: ForgeConfigSpec.ConfigValue<String>
    val DISCORD_REPLY: ForgeConfigSpec.ConfigValue<String>

    val RELOAD_ERROR: ForgeConfigSpec.ConfigValue<String>
    val RELOAD_SUCCESS: ForgeConfigSpec.ConfigValue<String>

    init {
        val builder = ForgeConfigSpec.Builder()

        GENERIC_ERROR = builder.comment("Generic error message sent to Discord")
            .define("genericError", "Something went wrong!")

        FAQ_ERROR = builder.comment("Message sent to Discord if an error ocurrs during FAQ loading")
            .define("faqError", "Bot Error (Contact Bot Operator)")
        FAQ_NO_REGISTERED =
            builder.comment(
                """Message sent to Discord when there is no FAQ for the given id.
                The placeholder {{id}} may be used to include the requested id""".trimIndent()
            )
                .define("faqNoRegistered", "No FAQ registered for id: `{{id}}`")

        TICK_REPORT =
            builder.comment("""Template for the tick report sent to users on the /tps command.
                the average time per tick in MSPT can be used via {{mspt}} and the TPS calculated
                from it via {{tps}}
            """.trimIndent())
                .define("tickReport", "Average Tick Time: {{mspt}} MSPT (TPS: {{tps}})")

        PLAYER_MESSAGE =
            builder.comment("""Template for how Minecraft chat messages are sent to Discord.
                The player's name can be accessed via {{name}} and its name with pre- and suffix
                via {{fullName}}. The message itself is accessed via {{message}}.
            """.trimIndent())
                .define(mutableListOf("messages", "minecraft"), "<{{fullName}}> {{message}}")

        DISCORD_MESSAGE =
            builder.comment("""Template for how Discord messages are synchronized to Minecraft.
                The sender is referenced by {{user}}. The optional response is accessed by {{reply}}
            """.trimIndent())
                .define(mutableListOf("messages", "discord", "message"), "{{user}}{{reply}}:")
        DISCORD_REPLY =
            builder.comment("""Template for the {{reply}} part of the discordMessage.
                The user the message is in reply to is referenced by {{replied}}
            """.trimIndent())
                .define(mutableListOf("messages", "discord", "reply"), " replying to {{replied}}:")

        RELOAD_ERROR =
            builder.comment("""Template for the error message sent to Discord when reloading fails.
                The error message itself is accessible with {{error}}
            """.trimIndent())
                .define(mutableListOf("reload", "error"), "Something went wrong: {{error}}")
        RELOAD_SUCCESS = builder.comment("Message sent to Discord after a successful reload")
            .define(mutableListOf("reload", "success"), "Reloaded commands for guild")

        SPEC = builder.build()
    }
}