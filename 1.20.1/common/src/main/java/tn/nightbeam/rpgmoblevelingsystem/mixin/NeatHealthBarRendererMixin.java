package tn.nightbeam.rpgmoblevelingsystem.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tn.nightbeam.rpgmoblevelingsystem.compat.NeatCompat;

/**
 * Appends the mob level into Neat's plate name (and plate width) when Neat is present.
 */
@Mixin(targets = "vazkii.neat.HealthBarRenderer")
public class NeatHealthBarRendererMixin {
    @Unique
    private static final ThreadLocal<LivingEntity> RPGMLS$CURRENT = new ThreadLocal<>();

    @Inject(method = "hookRender", at = @At("HEAD"))
    private static void rpgmls$captureEntity(
            Entity entity,
            PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            EntityRenderer<?> entityRenderer,
            float partialTicks,
            double x,
            double y,
            double z,
            CallbackInfo ci
    ) {
        if (entity instanceof LivingEntity living) {
            RPGMLS$CURRENT.set(living);
        } else {
            RPGMLS$CURRENT.remove();
        }
    }

    @Inject(method = "hookRender", at = @At("RETURN"))
    private static void rpgmls$clearEntity(
            Entity entity,
            PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            EntityRenderer<?> entityRenderer,
            float partialTicks,
            double x,
            double y,
            double z,
            CallbackInfo ci
    ) {
        RPGMLS$CURRENT.remove();
    }

    @ModifyArg(
            method = "hookRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I",
                    ordinal = 0
            ),
            index = 0
    )
    private static String rpgmls$widenName(String text) {
        return rpgmls$appendLevel(text);
    }

    @ModifyArg(
            method = "hookRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    ordinal = 0
            ),
            index = 0
    )
    private static String rpgmls$drawName(String text) {
        return rpgmls$appendLevel(text);
    }

    @Unique
    private static String rpgmls$appendLevel(String text) {
        LivingEntity living = RPGMLS$CURRENT.get();
        if (living == null || text == null || text.isEmpty()) {
            return text;
        }
        return NeatCompat.formatPlateName(living, text);
    }
}
