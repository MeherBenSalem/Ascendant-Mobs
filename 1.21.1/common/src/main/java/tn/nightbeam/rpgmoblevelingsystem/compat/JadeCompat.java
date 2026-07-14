package tn.nightbeam.rpgmoblevelingsystem.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.platform.Services;

/**
 * Jade integration hooks. When Jade is installed, level is exposed via entity tags
 * ({@code am_level_<n>}) which Jade resource plugins can read. This class also
 * provides formatted display helpers used by overlays and optional integrations.
 */
public final class JadeCompat {
    private JadeCompat() {
    }

    public static void registerIfPresent() {
        if (!ModConfig.global().jadeEnabled || !Services.PLATFORM.isModLoaded("jade")) {
            return;
        }
        Constants.LOG.info("Jade detected; mob levels are exposed via am_level_* entity tags");
    }

    public static Component formatEntityName(LivingEntity entity, Component baseName) {
        Integer level = MobLevelService.getDisplayLevel(entity.level(), entity);
        if (level == null) {
            return baseName;
        }
        if (entity.getCustomName() != null) {
            return baseName;
        }
        String format = ModConfig.global().nameFormat;
        if (format == null || format.isBlank()) {
            format = "%mob_name% [Lv. %level%]";
        }
        String text = format
                .replace("%mob_name%", baseName.getString())
                .replace("%level%", Integer.toString(level));
        return Component.literal(text);
    }

    public static Component levelLine(LivingEntity entity) {
        Integer level = MobLevelService.getDisplayLevel(entity.level(), entity);
        if (level == null) {
            return null;
        }
        return Component.literal("Level: " + level);
    }

    public static ResourceLocation uid() {
        return ResourceLocation.fromNamespaceAndPath("rpgmoblevelingsystem", "mob_level");
    }
}
