package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import net.minecraft.server.MinecraftServer
import java.util.UUID

class MinecraftHandler(private val server: MinecraftServer) {
    fun getOnlinePlayers(): Collection<GameProfile> {
        return server.playerList.players.map { it.gameProfile }
    }

    fun uuidToName(id: UUID): String? {
        val onlinePlayer = server.playerList.getPlayer(id)?.gameProfile
        return if (onlinePlayer != null) {
            onlinePlayer.name
        } else {
            null
        }
    }

    fun nameToUUID(name: String): UUID? {
        val onlinePlayer = server.playerList.getPlayerByName(name)?.gameProfile
        return if (onlinePlayer != null) {
            onlinePlayer.id
        } else {
            null
        }
    }
}