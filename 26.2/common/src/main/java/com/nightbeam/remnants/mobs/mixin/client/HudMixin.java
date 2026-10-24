package com.nightbeam.remnants.mobs.mixin.client;

import com.nightbeam.remnants.mobs.client.LensHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric: draws the lens readouts after the boss bars. NeoForge reworks this method and uses a GUI layer instead. */
@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow
    private boolean isHidden;

    @Inject(method = "extractRenderState", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void remnants$lens(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!isHidden) {
            LensHud.render(graphics);
        }
    }
}
