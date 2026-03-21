package tn.nightbeam.rpgmoblevelingsystem.fabric.gameplay;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tn.naizo.jauml.JaumlConfigLib;

public final class FabricMobLeveling {
    private static final String CONFIG_DIR = "rpgmoblevelingsystem";
    private static final String TAG_GOT_LEVEL = "am_got_level";
    private static final String TAG_LEVEL_PREFIX = "am_level_";

    private FabricMobLeveling() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (world.isClientSide()) {
                return;
            }
            applyMobLevel(world, entity);
        });
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
        double lockedLevel = getLockedLevel(entity);
        if (lockedLevel != -1) {
            return lockedLevel;
        }

        double existing = getStoredLevel(entity);
        if (existing > 0) {
            return existing;
        }

        double baseLevel = getBaseLevel(world);
        double maxLevel = getMaxLevel(world);
        double scaleFactor = JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "scale_factor");

        double level = Math.floor(baseLevel + calculateSpawnDifference(world, entity) * scaleFactor);
        return Math.min(level, maxLevel);
    }

    private static void applyConfiguredAttributes(Entity entity, LivingEntity living, double level) {
        int len = (int) JaumlConfigLib.getArrayLength(CONFIG_DIR, "attributes_settings", "attributes_list");
        for (int i = 0; i < len; i++) {
            String line = JaumlConfigLib.getArrayElement(CONFIG_DIR, "attributes_settings", "attributes_list", i);
            if (!line.contains("[attribute]") || !line.contains("[attributeEnd]") || !line.contains("[value]")
                    || !line.contains("[valueEnd]") || !line.contains("[max]") || !line.contains("[maxEnd]")) {
                continue;
            }

            String attributeId = between(line, "[attribute]", "[attributeEnd]");
            double value = parseDoubleSafe(between(line, "[value]", "[valueEnd]"));
            double maxValue = parseDoubleSafe(between(line, "[max]", "[maxEnd]"));
            String mobFilter = line.contains("[mob]") && line.contains("[mobEnd]") ? between(line, "[mob]", "[mobEnd]") : "";

            if (!mobFilter.isEmpty() && !entityTypeId(entity).equals(mobFilter)) {
                continue;
            }

            ResourceLocation key = ResourceLocation.tryParse(attributeId);
            if (key == null) {
                continue;
            }

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(key);
            if (attribute == null) {
                continue;
            }

            AttributeInstance instance = living.getAttribute(attribute);
            if (instance == null) {
                continue;
            }

            double current = instance.getBaseValue() + level * value;
            if (current > maxValue) {
                current = maxValue;
            }
            instance.setBaseValue(current);
        }
    }

    private static boolean isBanned(Entity entity) {
        return JaumlConfigLib.stringExistsInArray(CONFIG_DIR, "mobs_list_settings", "banned", entityTypeId(entity));
    }

    private static double getLockedLevel(Entity entity) {
        int len = (int) JaumlConfigLib.getArrayLength(CONFIG_DIR, "mobs_list_settings", "locked_mobs");
        for (int i = 0; i < len; i++) {
            String line = JaumlConfigLib.getArrayElement(CONFIG_DIR, "mobs_list_settings", "locked_mobs", i);
            if (line.contains(entityTypeId(entity)) && line.contains("/")) {
                return parseDoubleSafe(line.substring(0, line.indexOf('/')));
            }
        }
        return -1;
    }

    private static double calculateSpawnDifference(ServerLevel world, Entity entity) {
        String scaleType = JaumlConfigLib.getStringValue(CONFIG_DIR, "scale_settings", "scale_type");
        if ("vertical".equals(scaleType)) {
            return Math.floor(Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2)
                    / JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "scale_distance"));
        }
        if ("horizontal".equals(scaleType)) {
            double horizontal = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2)
                    + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2);
            return Math.floor(horizontal / JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "scale_distance"));
        }
        if ("time".equals(scaleType)) {
            double dayFactor = JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "day_factor");
            return Math.floor((world.dayTime() / 24000.0) / Math.max(1.0, dayFactor));
        }
        if ("random".equals(scaleType)) {
            int min = (int) Math.floor(getBaseLevel(world));
            int max = (int) Math.floor(getMaxLevel(world));
            if (max < min) {
                max = min;
            }
            return Mth.nextInt(RandomSource.create(), min, max);
        }

        double both = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2)
                + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2)
                + Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
        return Math.floor(both / JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "scale_distance"));
    }

    private static double getBaseLevel(Level world) {
        double toReturn = JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "base_level");
        String dimName = getDimensionId(world);
        int len = (int) JaumlConfigLib.getArrayLength(CONFIG_DIR, "dimensions_settings", "dimensions");

        for (int i = 0; i < len; i++) {
            String line = JaumlConfigLib.getArrayElement(CONFIG_DIR, "dimensions_settings", "dimensions", i);
            String dimensionPart = after(line, "[maxEnd]");
            if (dimensionPart != null && dimName.contains(dimensionPart)) {
                String min = between(line, "[min]", "[minEnd]");
                return parseDoubleSafe(min);
            }
        }

        return toReturn;
    }

    private static double getMaxLevel(Level world) {
        double toReturn = JaumlConfigLib.getNumberValue(CONFIG_DIR, "scale_settings", "base_level");
        String dimName = getDimensionId(world);
        int len = (int) JaumlConfigLib.getArrayLength(CONFIG_DIR, "dimensions_settings", "dimensions");

        for (int i = 0; i < len; i++) {
            String line = JaumlConfigLib.getArrayElement(CONFIG_DIR, "dimensions_settings", "dimensions", i);
            String dimensionPart = after(line, "[maxEnd]");
            if (dimensionPart != null && dimName.contains(dimensionPart)) {
                String max = between(line, "[max]", "[maxEnd]");
                return parseDoubleSafe(max);
            }
        }

        return toReturn;
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

    private static double getStoredLevel(Entity entity) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith(TAG_LEVEL_PREFIX)) {
                return parseDoubleSafe(tag.substring(TAG_LEVEL_PREFIX.length()));
            }
        }
        return 0;
    }

    private static void storeLevelTags(Entity entity, double level) {
        entity.getTags().removeIf(tag -> tag.startsWith(TAG_LEVEL_PREFIX));
        entity.addTag(TAG_LEVEL_PREFIX + Double.toString(level));
        entity.addTag(TAG_GOT_LEVEL);
    }

    private static String between(String value, String startToken, String endToken) {
        int start = value.indexOf(startToken);
        int end = value.indexOf(endToken);
        if (start < 0 || end < 0 || end <= start) {
            return "";
        }
        return value.substring(start + startToken.length(), end);
    }

    private static String after(String value, String token) {
        int start = value.indexOf(token);
        if (start < 0) {
            return null;
        }
        return value.substring(start + token.length());
    }

    private static String formatLevel(double value) {
        return new java.text.DecimalFormat("##").format(value);
    }

    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }
}
