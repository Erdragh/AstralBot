package dev.erdragh.astralbot.multiplatform

import net.minecraft.server.MinecraftServer
import java.io.File
import java.nio.file.Path

expect fun MinecraftServer.getAstralbotDirectory(): File