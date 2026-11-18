package com.nightbeam.remnants.mobs.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.mobs.client.Plate;
import com.nightbeam.remnants.mobs.client.Plates;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    protected abstract boolean shouldShowName(T entity);

    @Inject(method = "render", at = @At("HEAD"))
    private void remnants$plate(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        // Built once per render in EntityRenderDispatcherMixin; drawn there instead when this render never runs.
        Plate plate = Plates.take(entity);
        if (plate != null) {
            boolean replace = RemnantsConfig.client().hideVanillaName;
            Plates.render(plate, pose, buffers, entityRenderDispatcher.cameraOrientation(), !replace && shouldShowName(entity));
        }
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void remnants$hideNameTag(T entity, Component name, PoseStack pose, MultiBufferSource buffers, int light, float partialTick,
                                      CallbackInfo ci) {
        if (Plates.hidesNameTag(entity)) {
            ci.cancel();
        }
    }
}
