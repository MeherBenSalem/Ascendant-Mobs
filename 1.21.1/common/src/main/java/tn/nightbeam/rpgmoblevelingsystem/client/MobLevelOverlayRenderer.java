package tn.nightbeam.rpgmoblevelingsystem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;

public final class MobLevelOverlayRenderer {
    private static final float TEXT_SCALE = 0.11F;
    private static final double MAX_RENDER_DISTANCE_SQR = 48.0D * 48.0D;

    private MobLevelOverlayRenderer() {
    }

    public static void render(PoseStack poseStack, Camera camera, MultiBufferSource bufferSource, float partialTick, float offsetX, float offsetY, float offsetZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        if (minecraft.level.getGameTime() % 80 == 0) {
            int total = 0;
            for (Entity ignored : minecraft.level.entitiesForRendering()) {
                total++;
            }
            Constants.LOG.info("MobLevelOverlayRenderer.render called for {} entities", total);
        }

        int renderedCount = 0;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity instanceof Player || !living.isAlive()) {
                continue;
            }
            if (minecraft.player.distanceToSqr(entity) > MAX_RENDER_DISTANCE_SQR) {
                continue;
            }

            Integer level = MobLevelingLogic.getDisplayLevel(minecraft.level, entity);
            if (level == null) {
                continue;
            }

            String levelLabel = "Lv." + level;
            Component currentName = living.getCustomName();
            if (currentName == null || currentName.getString().startsWith("Lv.")) {
                if (currentName == null || !levelLabel.equals(currentName.getString())) {
                    living.setCustomName(Component.literal(levelLabel));
                }
                if (!living.isCustomNameVisible()) {
                    living.setCustomNameVisible(true);
                }
            }

            if (minecraft.level.getGameTime() % 80 == 0 && renderedCount < 3) {
                Constants.LOG.info("MobLevelOverlayRenderer: entity {} type={} level={} dist={} los={}", entity.getUUID(), entity.getType().getDescriptionId(), level, minecraft.player.distanceToSqr(entity), minecraft.player.hasLineOfSight(entity));
            }
            renderedCount++;

            double x = Mth.lerp(partialTick, entity.xOld, entity.getX()) - camera.getPosition().x + offsetX;
            double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) - camera.getPosition().y + entity.getBbHeight() + offsetY;
            double z = Mth.lerp(partialTick, entity.zOld, entity.getZ()) - camera.getPosition().z + offsetZ;
            
            poseStack.pushPose();
            poseStack.translate(x, y + 0.8, z);
            poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

            Font font = minecraft.font;
            Matrix4f matrix = poseStack.last().pose();
            String text = Integer.toString(level);
            float xOffset = -font.width(text) / 2.0F;
            int textColor = levelColor(level);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            font.drawInBatch(text, xOffset, 0, 0xFF2A2A2A, false, matrix, bufferSource, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
            font.drawInBatch(text, xOffset, 0, textColor, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();

            poseStack.popPose();
        }

    }
    private static int levelColor(int level) {
        float t = Mth.clamp(level / 40.0F, 0.0F, 1.0F);
        int red = (int) Mth.lerp(t, 40.0F, 230.0F);
        int green = (int) Mth.lerp(t, 245.0F, 60.0F);
        int blue = (int) Mth.lerp(t, 110.0F, 60.0F);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
