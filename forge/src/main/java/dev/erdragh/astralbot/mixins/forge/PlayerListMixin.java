package dev.erdragh.astralbot.mixins.forge;

import dev.erdragh.astralbot.forge.event.SystemMessageEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void mc$onBroadcast(Component message, ChatType chatType, UUID uuid, CallbackInfo ci) {
        astralbot$onBroadcast(message, chatType);
    }

    @Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"))
    private void mc$onBroadcast(Component message, Function<ServerPlayer, Component> filter, ChatType chatType, UUID uuid, CallbackInfo ci) {
        astralbot$onBroadcast(message, chatType);
    }

    @Unique
    private void astralbot$onBroadcast(Component message, ChatType chatType) {
        if (chatType == ChatType.SYSTEM) {
            MinecraftForge.EVENT_BUS.post(new SystemMessageEvent(message));
        }
    }
}