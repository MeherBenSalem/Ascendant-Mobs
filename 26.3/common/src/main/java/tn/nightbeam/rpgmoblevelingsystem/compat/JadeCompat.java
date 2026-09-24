package tn.nightbeam.rpgmoblevelingsystem.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
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
        if (!ModConfig.global().jadeEnabled || !Services.platform().isModLoaded("jade")) {
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
        return composeFormattedName(format, baseName, level);
    }

    private static Component composeFormattedName(String format, Component mobName, int level) {
        MutableComponent result = Component.empty();
        int index = 0;
        while (index < format.length()) {
            int mobIdx = format.indexOf("%mob_name%", index);
            int levelIdx = format.indexOf("%level%", index);
            if (mobIdx < 0 && levelIdx < 0) {
                result.append(Component.literal(format.substring(index)));
                break;
            }
            int nextIdx;
            if (mobIdx < 0) {
                nextIdx = levelIdx;
            } else if (levelIdx < 0) {
                nextIdx = mobIdx;
            } else {
                nextIdx = Math.min(mobIdx, levelIdx);
            }
            if (nextIdx > index) {
                result.append(Component.literal(format.substring(index, nextIdx)));
            }
            if (nextIdx == mobIdx) {
                result.append(mobName);
                index = mobIdx + "%mob_name%".length();
            } else {
                result.append(Component.literal(Integer.toString(level)));
                index = levelIdx + "%level%".length();
            }
        }
        return result;
    }

    public static Component levelLine(LivingEntity entity) {
        Integer level = MobLevelService.getDisplayLevel(entity.level(), entity);
        if (level == null) {
            return null;
        }
        return Component.literal("Level: " + level);
    }

    public static Identifier uid() {
        return Identifier.fromNamespaceAndPath("rpgmoblevelingsystem", "mob_level");
    }
}
