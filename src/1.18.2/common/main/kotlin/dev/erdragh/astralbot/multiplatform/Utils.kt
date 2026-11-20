package dev.erdragh.astralbot.multiplatform

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.MODID
import dev.erdragh.astralbot.handlers.DiscordMessageComponent
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.io.File
import java.util.UUID

actual fun MinecraftServer.getAstralbotDirectory(): File {
    return File(this.serverDirectory, MODID)
}

actual fun MinecraftServer.getAstralMaxPlayerCount(): Int {
    return this.maxPlayers
}

actual fun MinecraftServer.getAstralPlayerByUUID(uuid: UUID): GameProfile? {
    return this.profileCache[uuid].orElse(null)
}

actual fun MinecraftServer.getAstralPlayerByName(name: String): GameProfile? {
    return this.profileCache[name].orElse(null)
}

actual fun MinecraftServer.defaultPlayer(): ServerPlayer? {
    return getAstralPlayerByName("Notch")?.let { ServerPlayer(this, this.allLevels.elementAt(0), it) }
}

actual fun MinecraftServer.smoothTickTime(): Float {
    return this.averageTickTime
}

actual fun MinecraftServer.broadcastMessageToAllPlayers(message: Component) {
    this.playerList.broadcastMessage(DiscordMessageComponent(message), ChatType.SYSTEM, Util.NIL_UUID)
}

actual fun CommandSourceStack.astralSuccess(renderComponent: () -> MutableComponent) {
    this.sendSuccess(renderComponent(), false)
}