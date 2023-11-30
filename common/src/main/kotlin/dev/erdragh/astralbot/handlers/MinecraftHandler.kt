package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import java.util.UUID

object MinecraftHandler {
    fun getOnlinePlayers(): Collection<GameProfile>? {
        val server = Minecraft.getInstance().currentServer;

        return server?.players?.sample
    }

    fun uuidToName(id: UUID): String? {
        val onlinePlayer = getOnlinePlayers()?.find { it.id.equals(id) }
        return if (onlinePlayer != null) {
            onlinePlayer.name
        } else {
            null
        }
    }

    fun nameToUUID(name: String): UUID? {
        val onlinePlayer = getOnlinePlayers()?.find { it.name.equals(name) }
        return if (onlinePlayer != null) {
            onlinePlayer.id
        } else {
            null
        }
    }
}