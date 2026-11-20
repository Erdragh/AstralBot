package dev.erdragh.astralbot.util

import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import dev.erdragh.astralbot.multiplatform.Chat
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.EntityType
import org.commonmark.parser.Parser
import java.awt.Color
import java.util.regex.Pattern

// Pattern for recognizing a URL, based off RFC 3986
// Source: https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
val urlPattern: Pattern = Pattern.compile(
    "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)

fun formatMarkdownToComponent(md: String): MutableComponent {
    if (!AstralBotConfig.ENABLE_MARKDOWN_PARSING.get()) return Chat.literal(md)

    val parser = Parser.builder().build()
    val parsed = parser.parse(md)
    val renderer = ComponentRenderer()

    return renderer.renderToComponent(parsed)
}

fun formatComponentToMarkdown(comp: Component): String {
    return Chat.flatten(comp)
        .map {
            var formatted = it.string

            if (it.style.isBold) {
                formatted = "**$formatted**"
            }
            if (it.style.isItalic) {
                formatted = "_${formatted}_"
            }
            Chat.getOpenedUrl(it)?.let { url ->
                formatted = "[$formatted](${url})"
            }

            val matcher = urlPattern.matcher(formatted)
            val replaced = matcher.replaceAll { match ->
                val group = match.group()
                return@replaceAll if (AstralBotConfig.urlAllowed(group)) {
                    group
                } else {
                    AstralBotTextConfig.GENERIC_BLOCKED.get()
                }
            }
            formatted = replaced

            return@map formatted
        }
        .joinToString("")
}

fun formatHoverText(text: Component): MessageEmbed {
    return EmbedBuilder()
        .setDescription(text.string)
        .let { builder: EmbedBuilder ->
            text.style.color?.value?.let { color -> builder.setColor(color) }
            builder
        }
        .build()
}

fun formatHoverEntity(entity: HoverEvent.EntityTooltipInfo): MessageEmbed? {
    if (entity.type == EntityType.PLAYER) return null
    return EmbedBuilder()
        .setTitle(Chat.entityName(entity))
        .setDescription(entity.type.description.string)
        .let { builder: EmbedBuilder ->
            val mobCategory = entity.type.category
            if (mobCategory.isFriendly) {
                builder.setColor(Color.GREEN)
            }
            builder
        }
        .build()
}