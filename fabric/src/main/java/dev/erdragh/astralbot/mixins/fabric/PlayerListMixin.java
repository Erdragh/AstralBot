package dev.erdragh.astralbot.mixins.fabric;

import dev.erdragh.astralbot.fabric.event.ServerMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow public abstract ServerPlayer getPlayer(UUID playerUUID);

    @Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void mc$onBroadcast(Component message, ChatType chatType, UUID uuid, CallbackInfo ci) {
        onBroadcast(message, chatType, uuid);
    }

    @Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void mc$onBroadcast(Component message, Function<ServerPlayer, Component> filter, ChatType chatType, UUID uuid, CallbackInfo ci) {
        onBroadcast(message, chatType, uuid);
    }

    @Unique
    private void onBroadcast(Component message, ChatType chatType, UUID uuid) {
        if (chatType == ChatType.CHAT) {
            ServerMessageEvents.getCHAT_MESSAGE().invoker().onChatMessage(message, getPlayer(uuid), null);
        } else if (chatType == ChatType.SYSTEM) {
            ServerMessageEvents.getGAME_MESSAGE().invoker().onGameMessage(null, message, null);
        }
    }
}