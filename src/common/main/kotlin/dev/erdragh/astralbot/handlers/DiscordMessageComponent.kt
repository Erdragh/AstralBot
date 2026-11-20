package dev.erdragh.astralbot.handlers

import net.minecraft.network.chat.Component

/**
 * Wrapper [Component] implemented by delegation to allow filtering of
 * messages being sent to clients
 * @author Erdragh
 */
expect class DiscordMessageComponent(wrapped: Component) : Component {
}