package tn.nightbeam.rpgmoblevelingsystem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.rpgmoblevelingsystem.Constants;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ForgeMobLevelOverlay {
    private static final String CONFIG_DIR = "rpgmoblevelingsystem";
    private static final String LEGACY_CONFIG_DIR = "RpgMobLevelingSystem";

    private ForgeMobLevelOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        String dir = configDir();
        if (!JaumlConfigLib.getBooleanValue(dir, "global_settings", "use_legacy_hud")) {
            return;
        }
        float offsetX = (float) JaumlConfigLib.getNumberValue(dir, "global_settings", "overlay_x_offset");
        float offsetY = (float) JaumlConfigLib.getNumberValue(dir, "global_settings", "overlay_y_offset");
        float offsetZ = (float) JaumlConfigLib.getNumberValue(dir, "global_settings", "overlay_z_offset");
        MobLevelOverlayRenderer.render(poseStack, event.getCamera(), event.getPartialTick(), offsetX, offsetY, offsetZ);
    }

    private static String configDir() {
        if (JaumlConfigLib.arrayKeyExists(CONFIG_DIR, "global_settings", "use_legacy_hud")) {
            return CONFIG_DIR;
        }
        return LEGACY_CONFIG_DIR;
    }
}
