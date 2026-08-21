package tn.nightbeam.rpgmoblevelingsystem.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.compat.NeatCompat;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeMobLevelOverlay {
    private NeoForgeMobLevelOverlay() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        if (!NeatCompat.shouldUseLegacyHud()) {
            return;
        }
        float offsetX = (float) ModConfig.global().overlayXOffset;
        float offsetY = (float) ModConfig.global().overlayYOffset;
        float offsetZ = (float) ModConfig.global().overlayZOffset;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        MobLevelOverlayRenderer.render(event.getPoseStack(), event.getCamera(), partialTick, offsetX, offsetY, offsetZ);
    }
}
