package dev.erdragh.astralbot.mixins.forge;

import dev.erdragh.astralbot.forge.event.SystemMessageEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSendGameMessage(Component message, Function<ServerPlayer, Component> playerMessageFactory, boolean overlay, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new SystemMessageEvent(message));
    }
}