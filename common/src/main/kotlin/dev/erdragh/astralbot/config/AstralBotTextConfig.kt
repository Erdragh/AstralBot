package dev.erdragh.astralbot.config

import net.minecraftforge.common.ForgeConfigSpec

object AstralBotTextConfig {
    val SPEC: ForgeConfigSpec

    val GENERIC_ERROR: ForgeConfigSpec.ConfigValue<String>
    val GENERIC_SUCCESS: ForgeConfigSpec.ConfigValue<String>
    val GENERIC_BLOCKED: ForgeConfigSpec.ConfigValue<String>

    val FAQ_ERROR: ForgeConfigSpec.ConfigValue<String>
    val FAQ_NO_REGISTERED: ForgeConfigSpec.ConfigValue<String>

    val TICK_REPORT: ForgeConfigSpec.ConfigValue<String>

    val PLAYER_MESSAGE: ForgeConfigSpec.ConfigValue<String>

    val WEBHOOK_NAME_TEMPLATE: ForgeConfigSpec.ConfigValue<String>

    val DISCORD_MESSAGE: ForgeConfigSpec.ConfigValue<String>
    val DISCORD_REPLY: ForgeConfigSpec.ConfigValue<String>
    val DISCORD_EMBEDS: ForgeConfigSpec.ConfigValue<String>

    val RELOAD_ERROR: ForgeConfigSpec.ConfigValue<String>
    val RELOAD_SUCCESS: ForgeConfigSpec.ConfigValue<String>

    val WHITELIST_LINKED_NOT_ALLOWED: ForgeConfigSpec.ConfigValue<String>

    val LINK_NO_MINECRAFT: ForgeConfigSpec.ConfigValue<String>
    val LINK_MINECRAFT_TAKEN: ForgeConfigSpec.ConfigValue<String>
    val LINK_DISCORD_TAKEN: ForgeConfigSpec.ConfigValue<String>
    val LINK_SUCCESSFUL: ForgeConfigSpec.ConfigValue<String>
    val LINK_ERROR: ForgeConfigSpec.ConfigValue<String>

    val LINK_COMMAND_MESSAGE: ForgeConfigSpec.ConfigValue<String>
    val LINK_COMMAND_ALREADY_LINKED: ForgeConfigSpec.ConfigValue<String>

    val UNLINK_UNLINKED: ForgeConfigSpec.ConfigValue<String>
    val UNLINK_NOPERMS: ForgeConfigSpec.ConfigValue<String>

    init {
        val builder = ForgeConfigSpec.Builder()
        val whitespaceRegex = Regex("\n[ \t]+")

        GENERIC_ERROR = builder.comment("Generic error message sent to Discord")
            .define("genericError", "Something went wrong!")
        GENERIC_SUCCESS = builder.comment("Generic success message sent to Discord")
            .define("genericSuccess", "Success!")
        GENERIC_BLOCKED = builder.comment("Generic string that replaces blocked URLs/Links")
            .define("genericBlocked", "[BLOCKED]")

        FAQ_ERROR = builder.comment("Message sent to Discord if an error ocurrs during FAQ loading")
            .define(mutableListOf("faq", "error"), "Bot Error (Contact Bot Operator)")
        FAQ_NO_REGISTERED =
            builder.comment(
                """Message sent to Discord when there is no FAQ for the given id.
                The placeholder {{id}} may be used to include the requested id""".replace(whitespaceRegex, "\n")
            )
                .define(mutableListOf("faq", "notRegistered"), "No FAQ registered for id: `{{id}}`")

        TICK_REPORT =
            builder.comment("""Template for the tick report sent to users on the /tps command.
                the average time per tick in MSPT can be used via {{mspt}} and the TPS calculated
                from it via {{tps}}
            """.replace(whitespaceRegex, "\n"))
                .define("tickReport", "Average Tick Time: {{mspt}} MSPT (TPS: {{tps}})")

        PLAYER_MESSAGE =
            builder.comment("""Template for how Minecraft chat messages are sent to Discord if webhooks aren't used
                The player's name can be accessed via {{name}} and its name with pre- and suffix
                via {{fullName}}. The message itself is accessed via {{message}}.
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("messages", "minecraft"), "<{{fullName}}> {{message}}")

        WEBHOOK_NAME_TEMPLATE =
            builder.comment("""Template for how chat synchronization using Webhooks formats
                the message author's name.
                The player's primary name can be accessed via {{primary}} and the secondary name via {{secondary}}.
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("webhook", "name"), "{{primary}} ({{secondary}})")

        DISCORD_MESSAGE =
            builder.comment("""Template for how Discord messages are synchronized to Minecraft.
                The sender is referenced by {{user}}. The optional response is accessed by {{reply}}.
                The message itself is accessed with {{message}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("messages", "discord", "message"), "{{user}}{{reply}}: {{message}}")
        DISCORD_REPLY =
            builder.comment("""Template for the {{reply}} part of the discordMessage.
                The user the message is in reply to is referenced by {{replied}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("messages", "discord", "reply"), " replying to {{replied}}")
        DISCORD_EMBEDS =
            builder.comment("Template for the label of embeds of a message.")
                .define(mutableListOf("messages", "discord", "embeds"), "Embeds:")

        RELOAD_ERROR =
            builder.comment("""Template for the error message sent to Discord when reloading fails.
                The error message itself is accessible with {{error}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("reload", "error"), "Something went wrong: {{error}}")
        RELOAD_SUCCESS = builder.comment("Message sent to Discord after a successful reload")
            .define(mutableListOf("reload", "success"), "Reloaded commands for guild")

        WHITELIST_LINKED_NOT_ALLOWED = builder.comment("""
            The message the user gets shown if they are already linked, but have not
            yet been whitelisted by another means (i.e. being an operator, being on the vanilla whitelist, etc.)
            The Minecraft username is accessible via {{name}}
            """.replace(whitespaceRegex, "\n"))
            .define(listOf("whitelist", "linkedNotAllowed"), "Hi {{mc}}! You're already linked, but not yet whitelisted.")

        LINK_NO_MINECRAFT =
            builder.comment("""
                Message for when there's no Minecraft account associated with the given Link code.
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "noMinecraft"), "Couldn't find Minecraft account to link.")
        LINK_MINECRAFT_TAKEN =
            builder.comment("""
                Message for when the Minecraft account is already linked. This should never happen under
                normal circumstances.
                The Minecraft username is referenced by {{name}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "minecaftTaken"), "Minecraft username {{name}} already linked.")
        LINK_DISCORD_TAKEN =
            builder.comment("""
                Message for when a Discord account is already linked.
                The Discord user is referenced by {{name}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "discordTaken"), "{{name}} already linked.")
        LINK_SUCCESSFUL =
            builder.comment("""
                Message for when linking was successful.
                The Minecraft usernames can be accessed via {{mc}}. The Discord user via {{dc}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "successful"), "Linked {{dc}} to Minecraft username {{mc}}.")
        LINK_ERROR =
            builder.comment("""
                Message for when linking failed for some reason.
                The error message can be accessed via {{error}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "error"), "Failed to link: {{error}}")

        LINK_COMMAND_MESSAGE =
            builder.comment("""
                The message sent to a Minecraft user that requested a link code.
                The code is referenced via {{code}}
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "command", "message"), "Use this code to /link yourself on Discord: {{code}}")
        LINK_COMMAND_ALREADY_LINKED =
            builder.comment("""
                The message sent to a Minecraft user requesting a link code when they're already linked.
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("link", "command", "alreadyLinked"), "You're already linked!")

        UNLINK_UNLINKED =
            builder.comment("""
                The message sent to the /unlink issuer on successful unlink. The unlinked
                user's name can be referenced with {{name}}.
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("unlink", "unlinked"), "Unlinked {{name}}")
        UNLINK_NOPERMS =
            builder.comment("""
                The message sent to the /unlink issuer when the issuer doesn't have the permissions
                to unlink another user.
            """.replace(whitespaceRegex, "\n"))
                .define(mutableListOf("unlink", "noPerms"), "You don't have the permissions to unlink other users")

        SPEC = builder.build()
    }
}