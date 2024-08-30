package dev.erdragh.astralbot.util

import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import org.commonmark.parser.Parser
import java.awt.Color
import java.util.regex.Pattern
import kotlin.jvm.optionals.getOrNull

// Pattern for recognizing a URL, based off RFC 3986
// Source: https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
val urlPattern: Pattern = Pattern.compile(
    "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)

fun formatMarkdownToComponent(md: String): MutableComponent {
    if (!AstralBotConfig.ENABLE_MARKDOWN_PARSING.get()) return Component.literal(md)

    val parser = Parser.builder().build()
    val parsed = parser.parse(md)
    val renderer = ComponentRenderer()

    return renderer.renderToComponent(parsed)
}

fun formatComponentToMarkdown(comp: Component): String {
    return comp.toFlatList()
        .map {
            var formatted = it.string

            if (it.style.isBold) {
                formatted = "**$formatted**"
            }
            if (it.style.isItalic) {
                formatted = "_${formatted}_"
            }
            it.style.clickEvent?.let { clickEvent ->
                if (clickEvent.action == ClickEvent.Action.OPEN_URL) {
                    formatted = "[$formatted](${clickEvent.value})"
                }
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

fun formatHoverItems(stack: ItemStack, knownItems: MutableList<ItemStack>, player: Player?): MessageEmbed? {
    if (knownItems.contains(stack)) return null
    knownItems.add(stack)
    // TODO check if context needs fixing
    val tooltip = stack.getTooltipLines(player, TooltipFlag.Default.NORMAL).map(::formatComponentToMarkdown)
    return EmbedBuilder()
        .setTitle("${tooltip[0]} ${if (stack.count > 1) "(${stack.count})" else ""}")
        .setDescription(tooltip.drop(1).let {
            if (stack.hasCustomHoverName()) {
                listOf(stack.item.description.string).plus(it)
            } else it
        }.joinToString("\n"))
        .let { builder: EmbedBuilder ->
            stack.rarity.color.color?.let { color -> builder.setColor(color) }
            builder
        }
        .build()
}

fun formatHoverEntity(entity: HoverEvent.EntityTooltipInfo): MessageEmbed? {
    if (entity.type == EntityType.PLAYER) return null
    return EmbedBuilder()
        .setTitle(entity.name?.toFlatList()?.joinToString(transform = Component::getString))
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