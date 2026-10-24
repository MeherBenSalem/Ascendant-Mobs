package com.nightbeam.remnants.mobs.mixin;

import com.nightbeam.remnants.mobs.content.ModContent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The Hunter's Lens scopes like a spyglass. */
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
    private void remnants$lensScoping(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (!cir.getReturnValueZ() && self.isUsingItem() && ModContent.HUNTERS_LENS != null && self.getUseItem().is(ModContent.HUNTERS_LENS.get())) {
            cir.setReturnValue(true);
        }
    }
}
