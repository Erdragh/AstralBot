package dev.erdragh.astralbot.multiplatform

import dev.erdragh.astralbot.MODID
import net.minecraft.server.MinecraftServer
import java.io.File

actual fun MinecraftServer.getAstralbotDirectory(): File {
    return File(this.serverDirectory, MODID)
}