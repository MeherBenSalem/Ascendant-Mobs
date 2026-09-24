package tn.nightbeam.rpgmoblevelingsystem.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.EntityClassification;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.platform.Services;

/**
 * Soft integration with Neat unit frames.
 * Minecraft 26.3 has no Neat build, so the mixin/dependency is omitted. Presence
 * checks remain safe if a future Neat jar is dropped in.
 */
public final class NeatCompat {
    public static final String MOD_ID = "neat";

    private NeatCompat() {
    }

    public static boolean isPresent() {
        return Services.platform().isModLoaded(MOD_ID);
    }

    public static void registerIfPresent() {
        // No 26.3 Neat mixin/target; keep this a no-op so boot is safe without Neat.
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
     * Applies {@link ModConfig.GlobalSettings#nameFormat} to a plain name string.
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
