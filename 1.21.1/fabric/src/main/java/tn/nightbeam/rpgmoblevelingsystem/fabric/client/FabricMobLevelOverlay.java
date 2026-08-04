package tn.nightbeam.rpgmoblevelingsystem.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import tn.nightbeam.rpgmoblevelingsystem.client.MobLevelOverlayRenderer;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

public final class FabricMobLevelOverlay {
    private FabricMobLevelOverlay() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ModConfig.global().useLegacyHud) {
                return;
            }
            float offsetX = (float) ModConfig.global().overlayXOffset;
            float offsetY = (float) ModConfig.global().overlayYOffset;
            float offsetZ = (float) ModConfig.global().overlayZOffset;
            MultiBufferSource buffers = context.consumers();
            if (buffers == null) {
                MobLevelOverlayRenderer.render(context.matrixStack(), context.camera(), context.tickCounter().getGameTimeDeltaTicks(), offsetX, offsetY, offsetZ);
                return;
            }
            MobLevelOverlayRenderer.render(context.matrixStack(), context.camera(), buffers, context.tickCounter().getGameTimeDeltaTicks(), offsetX, offsetY, offsetZ);
            if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
                bufferSource.endBatch();
            }
        });
    }
}
