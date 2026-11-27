package dev.erdragh.astralbot.mixins.fabric;

import dev.erdragh.astralbot.fabric.event.ServerMessageEvents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
    @Shadow public abstract ServerPlayer getPlayer();

    @Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at = @At(target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", value = "INVOKE", shift = At.Shift.AFTER))
    private void astralbot$onChat(TextFilter.FilteredText text, CallbackInfo ci) {
        ServerMessageEvents.getCHAT_MESSAGE().invoker().onChatMessage(new TextComponent(text.getRaw()), getPlayer(), null);
    }
}