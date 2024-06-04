package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.handlers.WhitelistHandler
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object UserEventListener : ListenerAdapter() {
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        WhitelistHandler.unWhitelist(event.user)
    }
}