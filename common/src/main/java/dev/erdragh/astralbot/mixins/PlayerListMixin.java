package dev.erdragh.astralbot.mixins;

import com.mojang.authlib.GameProfile;
import dev.erdragh.astralbot.handlers.WhitelistHandler;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
  @Inject(method = "isWhiteListed", at = @At("RETURN"), cancellable = true)
  private void astralbot$isWhiteListed(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
    // comparing a Boolean object in a normal boolean statement may produce a NullPointerException if said Object is null
    cir.setReturnValue(Boolean.TRUE.equals(cir.getReturnValue()) || WhitelistHandler.INSTANCE.checkWhitelist(profile.getId()) != null);
  }
}
