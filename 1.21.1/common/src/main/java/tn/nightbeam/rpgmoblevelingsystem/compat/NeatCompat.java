package tn.nightbeam.rpgmoblevelingsystem.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.EntityClassification;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.platform.Services;

/**
 * Soft integration with Neat unit frames. When Neat is present, levels are appended
 * to plate names via mixin; the legacy floating-digit HUD is suppressed.
 */
public final class NeatCompat {
    public static final String MOD_ID = "neat";

    private NeatCompat() {
    }

    public static boolean isPresent() {
        return Services.platform().isModLoaded(MOD_ID);
    }

    public static void registerIfPresent() {
        if (!isPresent()) {
            return;
        }
        // Force-load Neat's renderer so mixin apply errors surface at boot, not on first plate.
        try {
            Class.forName("vazkii.neat.HealthBarRenderer");
            Constants.LOG.info("Neat detected; mob levels will appear on Neat plates and the legacy level HUD is disabled");
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            Constants.LOG.warn("Neat mod id is present but HealthBarRenderer could not be loaded; plate levels disabled", e);
        }
    }

    public static boolean shouldUseLegacyHud() {
        return ModConfig.global().useLegacyHud && !isPresent();
    }

    public static boolean isHudHidden(Entity entity) {
        var hideHudFor = ModConfig.global().hideHudFor;
        if (hideHudFor == null || hideHudFor.isEmpty()) {
            return false;
        }
        String entityId = EntityClassification.entityTypeId(entity);
        for (String entry : hideHudFor) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            if (entry.endsWith(":")) {
                if (entityId.startsWith(entry)) {
                    return true;
                }
            } else if (entityId.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies {@link ModConfig.GlobalSettings#nameFormat} to Neat's plain name string.
     */
    public static String formatPlateName(LivingEntity entity, String baseName) {
        if (baseName == null || isHudHidden(entity)) {
            return baseName;
        }
        Integer level = MobLevelService.getDisplayLevel(entity.level(), entity);
        if (level == null) {
            return baseName;
        }
        String format = ModConfig.global().nameFormat;
        if (format == null || format.isBlank()) {
            format = "%mob_name% [Lv. %level%]";
        }
        return format.replace("%mob_name%", baseName).replace("%level%", Integer.toString(level));
    }
}
