package dev.erdragh.astralbot.util

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.jda
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

object MessageSenderLookup {
    private val minecraftSkins: MutableMap<Player, ResolvedSenderInfo> = mutableMapOf()
    private val discordAvatars: MutableMap<Player, ResolvedSenderInfo> = mutableMapOf()

    fun getMessageSenderInfo(player: ServerPlayer?, discord: Boolean = false): ResolvedSenderInfo? {
        if (player == null) return null

        if (discord) {
            val cached = discordAvatars[player]
            if (cached != null) return cached

            val discordId = WhitelistHandler.checkWhitelist(player.gameProfile.id)
                ?: return ResolvedSenderInfo("Unlinked Account (${(player.displayName ?: player.name).string})", null)
            val discordUser = jda?.getUserById(discordId)
            if (discordUser == null) {
                // Lazily load the user info if it's not cached
                jda?.retrieveUserById(discordId)?.submit()?.whenComplete { user, error ->
                    if (error == null) {
                        discordAvatars[player] = ResolvedSenderInfo(user.effectiveName, user.avatarUrl)
                    } else {
                        LOGGER.error("Failed to retrieve user: $discordId for chat synchronization", error)
                    }
                }
                return ResolvedSenderInfo("Uncached User (${(player.displayName ?: player.name).string})", null)
            }

            return discordAvatars.computeIfAbsent(player) {
                ResolvedSenderInfo(discordUser.effectiveName, discordUser.avatarUrl)
            }
        }
        return minecraftSkins.computeIfAbsent(player) {
            ResolvedSenderInfo((player.displayName ?: player.name).string, "https://mc-heads.net/head/${player.gameProfile.id}")
        }
    }

    data class ResolvedSenderInfo(val name: String, val avatar: String?)
}