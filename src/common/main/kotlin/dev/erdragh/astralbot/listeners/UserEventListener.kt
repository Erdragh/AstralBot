package dev.erdragh.astralbot.listeners

import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.limpMode
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object UserEventListener : ListenerAdapter() {
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        if (limpMode) return
        WhitelistHandler.unWhitelist(event.user)
    }
}