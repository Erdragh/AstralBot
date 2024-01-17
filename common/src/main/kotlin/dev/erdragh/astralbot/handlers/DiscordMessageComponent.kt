package dev.erdragh.astralbot.handlers

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
import net.minecraft.util.FormattedCharSequence

/**
 * Wrapper [Component] implemented by delegation to allow filtering of
 * messages being sent to clients
 * @author Erdragh
 */
class DiscordMessageComponent(private val wrapped: Component) : TextComponent("") {
    override fun getStyle(): Style {
        return wrapped.style
    }

    override fun getText(): String {
        return if (wrapped is TextComponent) wrapped.text else contents
    }

    override fun getContents(): String {
        return wrapped.contents
    }

    override fun getSiblings(): MutableList<Component> {
        return wrapped.siblings
    }

    override fun getVisualOrderText(): FormattedCharSequence {
        return wrapped.visualOrderText
    }
}