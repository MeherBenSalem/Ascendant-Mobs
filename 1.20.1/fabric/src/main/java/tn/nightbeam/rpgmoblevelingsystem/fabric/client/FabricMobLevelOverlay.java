package tn.nightbeam.rpgmoblevelingsystem.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
            MobLevelOverlayRenderer.render(context.matrixStack(), context.camera(), context.tickDelta(), offsetX, offsetY, offsetZ);
        });
    }
}
