package tn.mbs.ascendantmobs.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import tn.mbs.ascendantmobs.init.AscendantMobsModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.neat.HealthBarRenderer;
import vazkii.neat.NeatConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.Camera;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import java.util.HexFormat;

@Mixin(value = HealthBarRenderer.class, remap = false)
public class HealthBarRendererMixin {
    
	@Inject(method = "hookRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", shift = At.Shift.AFTER, ordinal = 3), cancellable = false)
	private static void onHookRender(Entity entity, PoseStack poseStack, MultiBufferSource buffers,
	        Camera camera, EntityRenderer<? super Entity> entityRenderer,
	        float partialTicks, double x, double y, double z, CallbackInfo ci) {
	
	    if (!(entity instanceof LivingEntity living)) return;
	
	    AttributeInstance attr = living.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get());
	    if (attr == null || attr.getBaseValue() <= 0) return;
	
	    int level = (int) attr.getBaseValue();
	    Component levelComponent = Component.literal(""+level);
	
	    Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
			
	    float globalScale = 0.0267F;
	    float textScale = 1.5F;
	    float halfSize = Math.max(NeatConfig.instance.plateSize(), font.width(living.getDisplayName().getString()) * 0.5F / 2.0F + 10.0F);
	    float textX = -halfSize / textScale - 15.0F;
	    float textY = NeatConfig.instance.hpTextHeight() - 3F;
	
	    poseStack.pushPose();
	    poseStack.translate(textX, textY, 0F);
	    poseStack.scale(textScale, textScale, textScale);
	
	    float fixedBgWidth = 25.0F;
	    float bgHeight = font.lineHeight + 13.0F;
	
	    VertexConsumer bgBuilder = buffers.getBuffer(vazkii.neat.NeatRenderType.BAR_TEXTURE_TYPE);
	    Matrix4f bgMatrix = poseStack.last().pose();
	    bgBuilder.vertex(bgMatrix, -fixedBgWidth / 2.0F, -bgHeight / 2.0F, 0.01F).color(0, 0, 0, 60).uv(0.0F, 0.0F).uv2(0xF000F0).endVertex();
	    bgBuilder.vertex(bgMatrix, -fixedBgWidth / 2.0F, bgHeight / 2.0F, 0.01F).color(0, 0, 0, 60).uv(0.0F, 0.5F).uv2(0xF000F0).endVertex();
	    bgBuilder.vertex(bgMatrix, fixedBgWidth / 2.0F, bgHeight / 2.0F, 0.01F).color(0, 0, 0, 60).uv(1.0F, 0.5F).uv2(0xF000F0).endVertex();
	    bgBuilder.vertex(bgMatrix, fixedBgWidth / 2.0F, -bgHeight / 2.0F, 0.01F).color(0, 0, 0, 60).uv(1.0F, 0.0F).uv2(0xF000F0).endVertex();
	
	    poseStack.translate(0, 0, -0.02F);
	    Matrix4f matrix = poseStack.last().pose();
	    int textColor = HexFormat.fromHexDigits("FFFFFF");
	    float levelTextWidth = font.width(levelComponent);
	    font.drawInBatch(levelComponent, -levelTextWidth / 2.0F, -font.lineHeight / 2.0F, textColor, true, matrix, buffers, Font.DisplayMode.NORMAL, 0, LightTexture.pack(15, 15));
	
	    poseStack.popPose();
	}
}