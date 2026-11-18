package com.nightbeam.remnants.mobs.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.mobs.client.Plate;
import com.nightbeam.remnants.mobs.client.Plates;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every entity render goes through here, whichever renderer draws it, so plates do not depend on the entity's renderer
 * reaching {@code EntityRenderer#render} (Epic Fight cancels or replaces that render for the mobs it animates).
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    private static final String RENDER = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void remnants$beginPlate(Entity entity, double x, double y, double z, float yaw, float partialTick, PoseStack pose,
                                     MultiBufferSource buffers, int light, CallbackInfo ci) {
        Plates.begin(entity, partialTick, ((EntityRenderDispatcher) (Object) this).distanceToSqr(entity));
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void remnants$endPlate(Entity entity, double x, double y, double z, float yaw, float partialTick, PoseStack pose,
                                   MultiBufferSource buffers, int light, CallbackInfo ci) {
        Plate plate = Plates.end(entity);
        if (plate == null) {
            return;
        }
        // The renderer never drew it. The dispatcher has popped its pose, so move to the entity as it did: x, y, z plus
        // the renderer's offset, the pose EntityRenderer#render would have been given.
        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
        Vec3 offset = dispatcher.getRenderer(entity).getRenderOffset(entity, partialTick);
        pose.pushPose();
        pose.translate(x + offset.x, y + offset.y, z + offset.z);
        Plates.render(plate, pose, buffers, dispatcher.cameraOrientation(), false);
        pose.popPose();
    }
}
