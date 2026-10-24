package com.nightbeam.remnants.mobs.mob;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * What Remnants knows about one mob. Stored as a single entity tag
 * ({@code remnant_mobs:data|level|rank|trait+trait|seed|flags}) so it survives saving, conversions and every loader.
 */
public record MobData(int level, Rank rank, Set<Trait> traits, long seed, int flags) {
    /** Marked with a Brand of Challenge: doubled rewards. */
    public static final int BRANDED = 1;
    /** Called by a Packleader: no rank, no extra rewards. */
    public static final int MINION = 2;
    /** Packleader has already called its pack. */
    public static final int PACK_CALLED = 4;
    /** Came from a monster spawner or trial spawner. */
    public static final int SPAWNER = 8;
    /** A boss (#remnant_mobs:bosses): level for show, stats only with boss_stats. */
    public static final int BOSS = 16;
    /** Spawned during a Hunter's Moon. */
    public static final int MOONLIT = 32;
    /** Passive or neutral creature: a level, never a rank. */
    public static final int PASSIVE = 64;
    /** Only in sync packets: a Warded mob's shield is up. Never saved. */
    public static final int WARD_UP = 1 << 16;

    public static final String TAG_PREFIX = RemnantMobs.MOD_ID + ":data|";

    public MobData {
        traits = traits.isEmpty() ? EnumSet.noneOf(Trait.class) : EnumSet.copyOf(traits);
    }

    public boolean has(Trait trait) {
        return traits.contains(trait);
    }

    public boolean flag(int flag) {
        return (flags & flag) != 0;
    }

    public MobData withFlag(int flag, boolean on) {
        return new MobData(level, rank, traits, seed, on ? flags | flag : flags & ~flag);
    }

    public MobData withLevel(int newLevel) {
        return new MobData(Math.max(0, newLevel), rank, traits, seed, flags);
    }

    public MobData withRank(Rank newRank) {
        return new MobData(level, newRank, traits, seed, flags);
    }

    public MobData withTraits(Set<Trait> newTraits) {
        return new MobData(level, rank, newTraits, seed, flags);
    }

    public boolean ranked() {
        return rank != Rank.COMMON;
    }

    public int traitMask() {
        int mask = 0;
        for (Trait trait : traits) {
            mask |= 1 << trait.ordinal();
        }
        return mask;
    }

    public static Set<Trait> traitsFromMask(int mask) {
        EnumSet<Trait> set = EnumSet.noneOf(Trait.class);
        for (Trait trait : Trait.values()) {
            if ((mask & (1 << trait.ordinal())) != 0) {
                set.add(trait);
            }
        }
        return set;
    }

    /** Traits in a stable display order. */
    public List<Trait> traitList() {
        return new ArrayList<>(traits);
    }

    public String encode() {
        StringBuilder traitKeys = new StringBuilder();
        for (Trait trait : traits) {
            if (!traitKeys.isEmpty()) {
                traitKeys.append('+');
            }
            traitKeys.append(trait.key());
        }
        return TAG_PREFIX + level + "|" + rank.key() + "|" + traitKeys + "|" + Long.toHexString(seed) + "|" + flags;
    }

    public static @Nullable MobData decode(String tag) {
        if (!tag.startsWith(TAG_PREFIX)) {
            return null;
        }
        String[] parts = tag.substring(TAG_PREFIX.length()).split("\\|", -1);
        if (parts.length < 2) {
            return null;
        }
        try {
            int level = Math.max(0, Integer.parseInt(parts[0]));
            Rank rank = Rank.byKey(parts[1]);
            EnumSet<Trait> traits = EnumSet.noneOf(Trait.class);
            if (parts.length > 2 && !parts[2].isEmpty()) {
                for (String key : parts[2].split("\\+")) {
                    Trait trait = Trait.byKey(key);
                    if (trait != null) {
                        traits.add(trait);
                    }
                }
            }
            long seed = parts.length > 3 && !parts[3].isEmpty() ? Long.parseUnsignedLong(parts[3].toLowerCase(Locale.ROOT), 16) : 0L;
            int flags = parts.length > 4 && !parts[4].isEmpty() ? Integer.parseInt(parts[4]) : 0;
            return new MobData(level, rank == null ? Rank.COMMON : rank, traits, seed, flags);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
