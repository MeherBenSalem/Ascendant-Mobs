package tn.nightbeam.rpgmoblevelingsystem.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tn.nightbeam.rpgmoblevelingsystem.client.MobLevelOverlayRenderer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererNameTagMixin {
    @Inject(
            method = "extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V",
            at = @At("TAIL")
    )
    private void rpgmoblevelingsystem$attachLevelNameTag(
            Entity entity,
            EntityRenderState state,
            float partialTicks,
            double nameTagDistance,
            double belowNameDistance,
            CallbackInfo ci) {
        MobLevelOverlayRenderer.attachToRenderState(entity, state, partialTicks);
    }
}
