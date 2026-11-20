package dev.erdragh.astralbot.multiplatform

import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import java.io.File
import java.nio.file.Path

expect fun MinecraftServer.getAstralbotDirectory(): File

fun CommandSourceStack.astralSuccess(renderComponent: () -> MutableComponent): Unit {}