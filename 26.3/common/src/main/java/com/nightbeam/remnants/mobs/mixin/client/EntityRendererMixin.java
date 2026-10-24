package com.nightbeam.remnants.mobs.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.mobs.client.Plates;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void remnants$extractPlate(T entity, S state, float partialTicks, CallbackInfo ci) {
        Plates.extract(entity, state, partialTicks);
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void remnants$submitPlate(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Plates.submit(state, poseStack, collector, camera);
    }
}
