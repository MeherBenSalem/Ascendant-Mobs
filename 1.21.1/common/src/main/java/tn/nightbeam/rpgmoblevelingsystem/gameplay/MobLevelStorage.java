package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.world.entity.Entity;

public final class MobLevelStorage {
    public static final String TAG_GOT_LEVEL = "am_got_level";
    public static final String TAG_LEVEL_PREFIX = "am_level_";

    private MobLevelStorage() {
    }

    public static boolean hasBeenLeveled(Entity entity) {
        return entity.getTags().contains(TAG_GOT_LEVEL);
    }

    public static Double getStoredLevel(Entity entity) {
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

    public static void storeLevel(Entity entity, double level) {
        entity.getTags().removeIf(tag -> tag.startsWith(TAG_LEVEL_PREFIX));
        entity.addTag(TAG_LEVEL_PREFIX + level);
        entity.addTag(TAG_GOT_LEVEL);
    }

    public static int getDisplayLevel(Entity entity) {
        Double stored = getStoredLevel(entity);
        return stored == null ? 0 : (int) Math.floor(stored);
    }
}
