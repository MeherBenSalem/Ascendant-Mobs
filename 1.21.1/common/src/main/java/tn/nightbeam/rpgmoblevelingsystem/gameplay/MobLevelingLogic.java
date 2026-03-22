package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

import java.text.DecimalFormat;
import java.util.UUID;

public final class MobLevelingLogic {
    private static final String TAG_GOT_LEVEL = "am_got_level";
    private static final String TAG_LEVEL_PREFIX = "am_level_";

    private MobLevelingLogic() {
    }

    public static void onEntityLoaded(Level level, Entity entity) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        applyMobLevel(serverLevel, entity);
    }

    public static int adjustDroppedExperience(LivingEntity living, int originalXp) {
        double level = getStoredLevelOrNull(living) == null ? 0 : getStoredLevelOrNull(living);
        if (level <= 0) {
            return originalXp;
        }
        double multiplier = Math.max(1.0, ModConfig.scale().xpModifier);
        return (int) Math.floor(originalXp + (level * multiplier));
    }

    public static Integer getDisplayLevel(Level world, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player || isBanned(entity)) {
            return null;
        }

        Double stored = getStoredLevelOrNull(entity);
        if (stored != null) {
            return (int) Math.floor(stored);
        }

        Double lockedLevel = ModConfig.mobs().lockedMobs.get(entityTypeId(entity));
        if (lockedLevel != null) {
            return (int) Math.floor(lockedLevel);
        }

        double baseLevel = getBaseLevel(world);
        double maxLevel = getMaxLevel(world);
        double scaleFactor = ModConfig.scale().scaleFactor;
        double level = Math.floor(baseLevel + calculateSpawnDifference(world, entity) * scaleFactor);
        return (int) Math.floor(Math.min(level, maxLevel));
    }

    public static void sendCalendarMessage(Level world, Player player) {
        if (world.isClientSide()) {
            return;
        }

        String msg = "Current day: " + formatLevel(Math.floor(world.dayTime() / 24000.0))
                + " | Current Dimension: " + getDimensionId(world)
                + " | Current Min-Max Levels: " + returnMaxMinLevels(world);
        player.displayClientMessage(Component.literal(msg), false);
    }

    private static void applyMobLevel(ServerLevel world, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
            return;
        }

        if (entity.getTags().contains(TAG_GOT_LEVEL) || isBanned(entity)) {
            return;
        }

        double level = getInitialLevel(world, entity);
        applyConfiguredAttributes(entity, living, level);
        living.setHealth(living.getMaxHealth());

        storeLevelTags(entity, level);
    }

    private static double getInitialLevel(ServerLevel world, Entity entity) {
        Double lockedLevel = ModConfig.mobs().lockedMobs.get(entityTypeId(entity));
        if (lockedLevel != null) {
            return lockedLevel;
        }

        Double existing = getStoredLevelOrNull(entity);
        if (existing != null) {
            return existing;
        }

        double baseLevel = getBaseLevel(world);
        double maxLevel = getMaxLevel(world);
        double scaleFactor = ModConfig.scale().scaleFactor;

        double level = Math.floor(baseLevel + calculateSpawnDifference(world, entity) * scaleFactor);
        return Math.min(level, maxLevel);
    }

    private static void applyConfiguredAttributes(Entity entity, LivingEntity living, double level) {
        for (ModConfig.AttributeRule rule : ModConfig.attributes().attributes) {
            if (rule.mobFilter != null && !rule.mobFilter.isEmpty() && !entityTypeId(entity).equals(rule.mobFilter)) {
                continue;
            }

            var key = rule.attributeKey();
            if (key == null) {
                continue;
            }

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(key);
            if (attribute == null) {
                continue;
            }

            var attributeHolder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
            AttributeInstance instance = living.getAttribute(attributeHolder);
            if (instance == null) {
                continue;
            }

            double current = instance.getBaseValue() + level * rule.valuePerLevel;
            if (current > rule.maxValue) {
                current = rule.maxValue;
            }
            instance.setBaseValue(current);
        }
    }

    private static boolean isBanned(Entity entity) {
        return ModConfig.mobs().banned.contains(entityTypeId(entity));
    }

    private static double calculateSpawnDifference(Level world, Entity entity) {
        var spawnPos = world.getSharedSpawnPos();
        String scaleType = ModConfig.scale().scaleType;
        if ("vertical".equals(scaleType)) {
            return Math.floor(Math.pow(entity.getY() - spawnPos.getY(), 2)
                    / Math.max(1.0, ModConfig.scale().scaleDistance));
        }
        if ("horizontal".equals(scaleType)) {
            double horizontal = Math.pow(entity.getX() - spawnPos.getX(), 2)
                    + Math.pow(entity.getZ() - spawnPos.getZ(), 2);
            return Math.floor(horizontal / Math.max(1.0, ModConfig.scale().scaleDistance));
        }
        if ("time".equals(scaleType)) {
            return Math.floor((world.dayTime() / 24000.0) / Math.max(1.0, ModConfig.scale().dayFactor));
        }
        if ("random".equals(scaleType)) {
            int min = (int) Math.floor(getBaseLevel(world));
            int max = (int) Math.floor(getMaxLevel(world));
            if (max < min) {
                max = min;
            }
            int range = Math.max(1, (max - min) + 1);
            UUID id = entity.getUUID();
            int hash = Math.abs(id.hashCode());
            return min + (hash % range);
        }

        double both = Math.pow(entity.getX() - spawnPos.getX(), 2)
            + Math.pow(entity.getZ() - spawnPos.getZ(), 2)
            + Math.pow(entity.getY() - spawnPos.getY(), 2);
        return Math.floor(both / Math.max(1.0, ModConfig.scale().scaleDistance));
    }

    private static double getBaseLevel(Level world) {
        double fallback = ModConfig.scale().baseLevel;
        String dimName = getDimensionId(world);

        for (ModConfig.DimensionRange range : ModConfig.dimensions().dimensions) {
            if (dimName.contains(range.dimensionId)) {
                return range.min;
            }
        }
        return fallback;
    }

    private static double getMaxLevel(Level world) {
        double fallback = ModConfig.scale().baseLevel;
        String dimName = getDimensionId(world);

        for (ModConfig.DimensionRange range : ModConfig.dimensions().dimensions) {
            if (dimName.contains(range.dimensionId)) {
                return range.max;
            }
        }
        return fallback;
    }

    private static String returnMaxMinLevels(Level world) {
        return "[" + formatLevel(getBaseLevel(world)) + " - " + formatLevel(getMaxLevel(world)) + "]";
    }

    private static String getDimensionId(Level world) {
        return world.dimension().location().toString();
    }

    private static String entityTypeId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static Double getStoredLevelOrNull(Entity entity) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith(TAG_LEVEL_PREFIX)) {
                try {
                    return Double.parseDouble(tag.substring(TAG_LEVEL_PREFIX.length()));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static void storeLevelTags(Entity entity, double level) {
        entity.getTags().removeIf(tag -> tag.startsWith(TAG_LEVEL_PREFIX));
        entity.addTag(TAG_LEVEL_PREFIX + level);
        entity.addTag(TAG_GOT_LEVEL);
    }

    private static String formatLevel(double value) {
        return new DecimalFormat("##").format(value);
    }
}
