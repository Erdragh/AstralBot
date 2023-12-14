package dev.erdragh.astralbot.mixins;

import com.mojang.authlib.GameProfile;
import dev.erdragh.astralbot.handlers.WhitelistHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "isWhiteListed", at = @At("RETURN"), cancellable = true)
    void astralbot$isWhiteListed(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WhitelistHandler.INSTANCE.handleLoginAttempt(profile.getId(), Boolean.TRUE.equals(cir.getReturnValue())));
    }

    @Inject(method = "canPlayerLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/TranslatableComponent;<init>(Ljava/lang/String;)V", ordinal = 0), cancellable = true)
    private void astralbot$returnWhiteListMessage(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(WhitelistHandler.INSTANCE.writeWhitelistMessage(gameProfile));
    }
}