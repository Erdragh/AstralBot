package dev.erdragh.astralbot.handlers

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

actual class DiscordMessageComponent : Component {
    private val wrapped: Component

    actual constructor(wrapped: Component) {
        this.wrapped = wrapped
    }

    override fun getStyle(): Style {
        return wrapped.style
    }

    override fun getContents(): String {
        return wrapped.contents
    }

    override fun getSiblings(): MutableList<Component> {
        return wrapped.siblings
    }

    override fun plainCopy(): MutableComponent {
        return wrapped.plainCopy()
    }

    override fun copy(): MutableComponent {
        return wrapped.copy()
    }

    override fun getVisualOrderText(): FormattedCharSequence {
        return wrapped.visualOrderText
    }
}