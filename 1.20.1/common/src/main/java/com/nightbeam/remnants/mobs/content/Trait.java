package com.nightbeam.remnants.mobs.content;

import com.nightbeam.remnants.mobs.RemnantMobs;
import java.util.Locale;
import javax.annotation.Nullable;

/** Abilities ranked mobs roll. Numbers live in traits.json; the order here is the network id and must not change. */
public enum Trait {
    VAMPIRIC("vampiric", 0xD23246),
    FROSTBITE("frostbite", 0x8FD8FF),
    INFERNAL("infernal", 0xFF7A1A),
    VENOMOUS("venomous", 0x6CCB3A),
    WITHERING("withering", 0x8C8C9C),
    BERSERK("berserk", 0xE8492E),
    CRUSHING("crushing", 0xC49A62),
    JUGGERNAUT("juggernaut", 0xA6A6B8),
    THORNSKIN("thornskin", 0x4FAF48),
    WARDED("warded", 0xF2D46B),
    REGENERATING("regenerating", 0xF58FB0),
    DEFLECTING("deflecting", 0xC8E8FF),
    SWIFT("swift", 0x9FE8E0),
    BLINKING("blinking", 0xB060FF),
    PACKLEADER("packleader", 0xE0A85C),
    VOLATILE("volatile", 0xFF5A2E);

    private final String key;
    private final int color;

    Trait(String key, int color) {
        this.key = key;
        this.color = color;
    }

    public String key() {
        return key;
    }

    public int color() {
        return color;
    }

    public String translationKey() {
        return "trait." + RemnantMobs.MOD_ID + "." + key;
    }

    public String descriptionKey() {
        return translationKey() + ".desc";
    }

    /** Pairs that cannot appear on the same mob. */
    public boolean conflictsWith(Trait other) {
        return pair(this, other, INFERNAL, FROSTBITE) || pair(this, other, SWIFT, JUGGERNAUT);
    }

    private static boolean pair(Trait a, Trait b, Trait x, Trait y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    public static @Nullable Trait byKey(String key) {
        String wanted = key.toLowerCase(Locale.ROOT);
        for (Trait trait : values()) {
            if (trait.key.equals(wanted)) {
                return trait;
            }
        }
        return null;
    }

    public static @Nullable Trait byId(int id) {
        Trait[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }
}
