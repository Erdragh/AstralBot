package dev.erdragh.astralbot.multiplatform

import com.mojang.authlib.GameProfile
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.io.File
import java.nio.file.Path
import java.util.UUID

expect fun MinecraftServer.getAstralbotDirectory(): File

expect fun MinecraftServer.getAstralMaxPlayerCount(): Int

expect fun MinecraftServer.getAstralPlayerByUUID(uuid: UUID): GameProfile?

expect fun MinecraftServer.getAstralPlayerByName(name: String): GameProfile?

expect fun MinecraftServer.defaultPlayer(): ServerPlayer?

expect fun MinecraftServer.smoothTickTime(): Float

expect fun MinecraftServer.broadcastMessageToAllPlayers(message: Component)

expect fun CommandSourceStack.astralSuccess(renderComponent: () -> MutableComponent)