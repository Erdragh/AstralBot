package dev.erdragh.astralbot.forge.event

import net.minecraft.network.chat.Component
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

@Cancelable
class SystemMessageEvent(var message: Component) : Event()