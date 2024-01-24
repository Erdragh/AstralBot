package dev.erdragh.astralbot.commands.minecraft

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import dev.erdragh.astralbot.handlers.WhitelistHandler
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

fun registerMinecraftCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
    dispatcher.register(literal<CommandSourceStack?>("link").executes(LinkCommand))
}

object LinkCommand : Command<CommandSourceStack> {
    override fun run(context: CommandContext<CommandSourceStack>?): Int {
        val caller = context?.source?.playerOrException!!
        val whitelistCode = WhitelistHandler.getOrGenerateWhitelistCode(caller.uuid)
        context.source.sendSuccess({
            Component.literal("Use this code to /link yourself on Discord:")
                .append(Component.literal("$whitelistCode").withStyle { it.withItalic(true) })
        }, false)
        return 0
    }
}