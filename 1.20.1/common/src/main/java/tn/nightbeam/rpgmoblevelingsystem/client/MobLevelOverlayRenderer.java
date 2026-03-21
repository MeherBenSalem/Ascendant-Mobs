package tn.nightbeam.rpgmoblevelingsystem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public final class MobLevelOverlayRenderer {
    private static final float TEXT_SCALE = 0.025F;
    private static final double MAX_RENDER_DISTANCE_SQR = 48.0D * 48.0D;
    private static final String TAG_LEVEL_PREFIX = "am_level_";

    private MobLevelOverlayRenderer() {
    }

    public static void render(PoseStack poseStack, Camera camera, float partialTick, float offsetX, float offsetY, float offsetZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity instanceof Player || !living.isAlive()) {
                continue;
            }
            if (minecraft.player.distanceToSqr(entity) > MAX_RENDER_DISTANCE_SQR || !minecraft.player.hasLineOfSight(entity)) {
                continue;
            }

            Integer level = readLevel(entity);
            if (level == null) {
                continue;
            }

            double x = Mth.lerp(partialTick, entity.xOld, entity.getX()) - camera.getPosition().x + offsetX;
            double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) - camera.getPosition().y + entity.getBbHeight() + offsetY;
            double z = Mth.lerp(partialTick, entity.zOld, entity.getZ()) - camera.getPosition().z + offsetZ;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

            Font font = minecraft.font;
            Matrix4f matrix = poseStack.last().pose();
            String text = Integer.toString(level);
            float xOffset = -font.width(text) / 2.0F;
            int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            int textColor = levelColor(level);

            font.drawInBatch(text, xOffset, 0, 0x2A2A2A, false, matrix, bufferSource, Font.DisplayMode.SEE_THROUGH, background, LightTexture.FULL_BRIGHT);
            font.drawInBatch(text, xOffset, 0, textColor, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    private static Integer readLevel(Entity entity) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith(TAG_LEVEL_PREFIX)) {
                try {
                    return (int) Math.floor(Double.parseDouble(tag.substring(TAG_LEVEL_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static int levelColor(int level) {
        float t = Mth.clamp(level / 40.0F, 0.0F, 1.0F);
        int red = (int) Mth.lerp(t, 40.0F, 230.0F);
        int green = (int) Mth.lerp(t, 245.0F, 60.0F);
        int blue = (int) Mth.lerp(t, 110.0F, 60.0F);
        return (red << 16) | (green << 8) | blue;
    }
}