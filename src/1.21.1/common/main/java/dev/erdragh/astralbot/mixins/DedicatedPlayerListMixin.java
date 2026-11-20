package dev.erdragh.astralbot.mixins;

import com.mojang.authlib.GameProfile;
import dev.erdragh.astralbot.handlers.WhitelistHandler;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedPlayerList.class)
public class DedicatedPlayerListMixin {
    @Inject(method = "isWhiteListed", at = @At("RETURN"), cancellable = true)
    void astralbot$isWhiteListed(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WhitelistHandler.INSTANCE.handleLoginAttempt(profile.getId(), Boolean.TRUE.equals(cir.getReturnValue())));
    }
}
