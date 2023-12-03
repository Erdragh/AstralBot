package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import net.minecraft.server.MinecraftServer
import java.util.*
import kotlin.jvm.optionals.getOrNull

class MinecraftHandler(private val server: MinecraftServer) {
    fun getOnlinePlayers(): Collection<GameProfile> {
        return server.playerList.players.map { it.gameProfile }
    }

    fun byUUID(id: UUID): GameProfile? {
        return server.profileCache?.get(id)?.getOrNull()
    }

    fun byName(name: String): GameProfile? {
        return server.profileCache?.get(name)?.getOrNull()
    }
}