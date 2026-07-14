package tn.nightbeam.rpgmoblevelingsystem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ForgeMobLevelOverlay {
    private ForgeMobLevelOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        if (!ModConfig.global().useLegacyHud) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        float offsetX = (float) ModConfig.global().overlayXOffset;
        float offsetY = (float) ModConfig.global().overlayYOffset;
        float offsetZ = (float) ModConfig.global().overlayZOffset;
        MobLevelOverlayRenderer.render(poseStack, event.getCamera(), event.getPartialTick(), offsetX, offsetY, offsetZ);
    }
}
