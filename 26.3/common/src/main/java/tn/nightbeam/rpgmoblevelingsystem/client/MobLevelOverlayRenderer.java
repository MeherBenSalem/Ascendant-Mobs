package tn.nightbeam.rpgmoblevelingsystem.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.rpgmoblevelingsystem.compat.NeatCompat;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;

/**
 * 26.3 world rendering uses the submit/extract pipeline instead of Font.drawInBatch.
 * Levels are attached to vanilla name tags so they still appear above mobs.
 */
public final class MobLevelOverlayRenderer {
    private static final double MAX_RENDER_DISTANCE_SQR = 48.0D * 48.0D;

    private MobLevelOverlayRenderer() {
    }

    public static void attachToRenderState(Entity entity, EntityRenderState state, float partialTick) {
        if (!NeatCompat.shouldUseLegacyHud()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (!(entity instanceof LivingEntity) || entity instanceof Player || !entity.isAlive()) {
            return;
        }
        if (minecraft.player.distanceToSqr(entity) > MAX_RENDER_DISTANCE_SQR) {
            return;
        }
        if (NeatCompat.isHudHidden(entity)) {
            return;
        }

        Integer level = MobLevelService.getDisplayLevel(minecraft.level, entity);
        if (level == null) {
            return;
        }

        if (ModConfig.global().overlayDebug && minecraft.level.getGameTime() % 200 == 0) {
            tn.nightbeam.rpgmoblevelingsystem.Constants.LOG.debug("Overlay level {} for {}", level, entity.getType().getDescriptionId());
        }

        Component levelText = Component.literal(Integer.toString(level)).withColor(levelColor(level));
        if (state.nameTag != null) {
            state.nameTag = state.nameTag.copy().append(Component.literal(" ")).append(levelText);
        } else {
            state.nameTag = levelText;
        }
        if (state.nameTagAttachment == null) {
            state.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTick));
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
