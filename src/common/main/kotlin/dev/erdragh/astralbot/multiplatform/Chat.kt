package dev.erdragh.astralbot.multiplatform

import net.dv8tion.jda.api.entities.MessageEmbed
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

expect object Chat {
    fun literal(of: String): MutableComponent
    fun empty(): MutableComponent

    fun handleHoverEvents(of: Component, handleText: (Component) -> Unit, handleItemStack: (ItemStack) -> Unit, handleEntity: (HoverEvent.EntityTooltipInfo) -> Unit)

    fun <T> hoverEvent(action: HoverEvent.Action<T>, component: T): HoverEvent
    fun clickEvent(action: ClickEvent.Action, value: String): ClickEvent

    fun flatten(from: Component): List<Component>
    fun getOpenedUrl(from: Component): String?
    fun entityName(from: HoverEvent.EntityTooltipInfo): String?

    fun formatHoverItems(stack: ItemStack, knownItems: MutableList<ItemStack>, player: Player?): MessageEmbed?

    fun wrapFromDiscord(component: Component): Component
    fun isFromDiscord(component: Component): Boolean
}

