package tn.nightbeam.rpgmoblevelingsystem.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.rpgmoblevelingsystem.client.MobLevelOverlayRenderer;

public final class FabricMobLevelOverlay {
    private static final String CONFIG_DIR = "rpgmoblevelingsystem";
    private static final String LEGACY_CONFIG_DIR = "RpgMobLevelingSystem";

    private FabricMobLevelOverlay() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            String dir = configDir();
            if (!JaumlConfigLib.getBooleanValue(dir, "global_settings", "use_legacy_hud")) {
                return;
            }
            float offsetX = (float) JaumlConfigLib.getNumberValue(dir, "global_settings", "overlay_x_offset");
            float offsetY = (float) JaumlConfigLib.getNumberValue(dir, "global_settings", "overlay_y_offset");
            float offsetZ = (float) JaumlConfigLib.getNumberValue(dir, "global_settings", "overlay_z_offset");
            MobLevelOverlayRenderer.render(context.matrixStack(), context.camera(), context.tickDelta(), offsetX, offsetY, offsetZ);
        });
    }

    private static String configDir() {
        if (JaumlConfigLib.arrayKeyExists(CONFIG_DIR, "global_settings", "use_legacy_hud")) {
            return CONFIG_DIR;
        }
        return LEGACY_CONFIG_DIR;
    }
}
