package dev.erdragh.astralbot.util

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.handlers.WhitelistHandler
import dev.erdragh.astralbot.jda
import net.dv8tion.jda.api.entities.User
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

object MessageSenderLookup {
    private val minecraftSkins: MutableMap<Player, ResolvedSenderInfo> = mutableMapOf()
    private val discordAvatars: MutableMap<Player, ResolvedSenderInfo> = mutableMapOf()

    fun getMessageSenderInfo(player: ServerPlayer?, discord: Boolean = false): ResolvedSenderInfo? {
        if (player == null) return null

        val discordId = WhitelistHandler.checkWhitelist(player.gameProfile.id)
        var discordUser: User? = null
        if (discordId != null) {
            discordUser = jda?.getUserById(discordId)
            if (discordUser == null) {
                // Lazily load the user info if it's not cached
                jda?.retrieveUserById(discordId)?.submit()?.whenComplete { user, error ->
                    if (error == null) {
                        discordAvatars[player] = ResolvedSenderInfo(user.effectiveName, (player.displayName ?: player.name).string, user.avatarUrl)
                        minecraftSkins[player] = ResolvedSenderInfo(
                            (player.displayName ?: player.name).string,
                            user.effectiveName,
                            AstralBotConfig.WEBHOOK_MC_AVATAR_URL.get()
                                .replace("{{uuid}}", player.gameProfile.id.toString())
                                .replace("{{name}}", player.gameProfile.name)
                        )
                    } else {
                        LOGGER.error("Failed to retrieve user: $discordId for chat synchronization", error)
                    }
                }
            }
        }

        if (discord) {
            val cached = discordAvatars[player]
            if (cached != null) return cached
            if (discordUser == null) {
                return ResolvedSenderInfo("Uncached", (player.displayName ?: player.name).string, null)
            }

            return discordAvatars.computeIfAbsent(player) {
                ResolvedSenderInfo(discordUser.effectiveName, (player.displayName ?: player.name).string, discordUser.avatarUrl)
            }
        }
        return minecraftSkins.computeIfAbsent(player) {
            ResolvedSenderInfo(
                (player.displayName ?: player.name).string,
                if (discordId == null) null else (discordUser?.effectiveName ?: "Uncached"),
                AstralBotConfig.WEBHOOK_MC_AVATAR_URL.get()
                    .replace("{{uuid}}", player.gameProfile.id.toString())
                    .replace("{{name}}", player.gameProfile.name)
            )
        }
    }

    data class ResolvedSenderInfo(val primaryName: String, val secondaryName: String?, val avatar: String?)
}