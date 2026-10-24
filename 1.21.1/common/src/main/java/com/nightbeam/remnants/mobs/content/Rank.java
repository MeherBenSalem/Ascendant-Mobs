package com.nightbeam.remnants.mobs.content;

import com.nightbeam.remnants.mobs.RemnantMobs;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/** How dangerous a leveled mob is beyond its level. Common mobs have no rank frame and no traits. */
public enum Rank {
    COMMON("common", 0xCFC8B8),
    VETERAN("veteran", 0x9BC46A),
    ELITE("elite", 0x5FA8E8),
    CHAMPION("champion", 0xB57BEB),
    REMNANT("remnant", 0xF0A43A);

    private final String key;
    private final int color;

    Rank(String key, int color) {
        this.key = key;
        this.color = color;
    }

    public String key() {
        return key;
    }

    /** RGB colour of the rank's crest, name and health bar. */
    public int color() {
        return color;
    }

    public int tier() {
        return ordinal();
    }

    public String translationKey() {
        return "rank." + RemnantMobs.MOD_ID + "." + key;
    }

    /** Entity tag every mob of this rank carries, for datapack selectors such as {@code @e[tag=remnant_mobs.elite]}. */
    public String entityTag() {
        return RemnantMobs.MOD_ID + "." + key;
    }

    public Rank next() {
        return this == REMNANT ? REMNANT : values()[ordinal() + 1];
    }

    public boolean atLeast(Rank other) {
        return ordinal() >= other.ordinal();
    }

    public static @Nullable Rank byKey(String key) {
        String wanted = key.toLowerCase(Locale.ROOT);
        for (Rank rank : values()) {
            if (rank.key.equals(wanted)) {
                return rank;
            }
        }
        return null;
    }

    public static Rank byTier(int tier) {
        Rank[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, tier))];
    }
}
