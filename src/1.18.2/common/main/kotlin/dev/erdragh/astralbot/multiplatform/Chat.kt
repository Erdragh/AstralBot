package dev.erdragh.astralbot.multiplatform

import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import dev.erdragh.astralbot.util.formatComponentToMarkdown
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

actual object Chat {
    actual fun literal(of: String): MutableComponent {
        return TextComponent(of)
    }
    actual fun empty(): MutableComponent {
        return literal("")
    }

    actual fun handleHoverEvents(of: Component, handleText: (Component) -> Unit, handleItemStack: (ItemStack) -> Unit, handleEntity: (HoverEvent.EntityTooltipInfo) -> Unit) {
        val attachments = of.toFlatList(of.style).mapNotNull {
            it.style.hoverEvent
        }

        for (attachment in attachments) {
            attachment.getValue(HoverEvent.Action.SHOW_TEXT)?.let(handleText)
            attachment.getValue(HoverEvent.Action.SHOW_ITEM)?.itemStack?.let(handleItemStack)
            attachment.getValue(HoverEvent.Action.SHOW_ENTITY)?.let(handleEntity)
        }
    }

    actual fun <T> hoverEvent(action: HoverEvent.Action<T>, component: T): HoverEvent {
        return HoverEvent(action, component)
    }
    actual fun clickEvent(action: ClickEvent.Action, value: String): ClickEvent {
        return ClickEvent(action, value)
    }

    actual fun flatten(from: Component): List<Component> {
        return from.toFlatList(from.style)
    }
    actual fun getOpenedUrl(from: Component): String? {
        return from.style.clickEvent?.let {
            if (it.action != ClickEvent.Action.OPEN_URL) return@let null
            return it.value
        }
    }
    actual fun entityName(from: HoverEvent.EntityTooltipInfo): String? {
        return from.name?.contents
    }


    actual fun formatHoverItems(stack: ItemStack, knownItems: MutableList<ItemStack>, player: Player?): MessageEmbed? {
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

    actual fun wrapFromDiscord(component: Component): Component {
        return DiscordMessageComponent(component)
    }

    actual fun isFromDiscord(component: Component) = component is DiscordMessageComponent
}