package tn.nightbeam.rpgmoblevelingsystem.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import tn.nightbeam.rpgmoblevelingsystem.client.MobLevelOverlayRenderer;
import tn.nightbeam.rpgmoblevelingsystem.compat.NeatCompat;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

public final class FabricMobLevelOverlay {
    private FabricMobLevelOverlay() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!NeatCompat.shouldUseLegacyHud()) {
                return;
            }
            float offsetX = (float) ModConfig.global().overlayXOffset;
            float offsetY = (float) ModConfig.global().overlayYOffset;
            float offsetZ = (float) ModConfig.global().overlayZOffset;
            float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(false);
            MultiBufferSource buffers = context.consumers();
            if (buffers == null) {
                MobLevelOverlayRenderer.render(context.matrixStack(), context.camera(), partialTick, offsetX, offsetY, offsetZ);
                return;
            }
            MobLevelOverlayRenderer.render(context.matrixStack(), context.camera(), buffers, partialTick, offsetX, offsetY, offsetZ);
            if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
                bufferSource.endBatch();
            }
        });
    }
}
