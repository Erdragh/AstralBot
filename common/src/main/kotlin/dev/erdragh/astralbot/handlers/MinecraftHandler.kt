package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import net.minecraft.server.MinecraftServer
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Wrapper class around the [MinecraftServer] to provide convenience
 * methods for fetching [GameProfile]s
 * @author Erdragh
 */
class MinecraftHandler(private val server: MinecraftServer) {
    /**
     * Fetches all currently online players' [GameProfile]s
     * @return a [Collection] of all currently online players'
     * [GameProfile]s
     */
    fun getOnlinePlayers(): Collection<GameProfile> {
        return server.playerList.players.map { it.gameProfile }
    }

    /**
     * Fetches the [GameProfile] of a given Minecraft user ID
     * @param id a User ID possibly associated with a Minecraft Account
     * @return the [GameProfile] of the given [id] or `null`
     * if:
     * - The given [id] doesn't have an actual Minecraft account
     *   associated with it
     * - The [server]'s profile cache hasn't been initialized yet
     */
    fun byUUID(id: UUID): GameProfile? {
        return server.profileCache?.get(id)?.getOrNull()
    }

    /**
     * Fetches the [GameProfile] of a given Minecraft username
     * @param name a username possibly associated with a Minecraft Account
     * @return the [GameProfile] of the given [name] or `null`
     * if:
     * - The given [name] doesn't have an actual Minecraft account
     *   associated with it
     * - The [server]'s profile cache hasn't been initialized yet
     */
    fun byName(name: String): GameProfile? {
        return server.profileCache?.get(name)?.getOrNull()
    }
}