package com.nightbeam.remnants.mobs.config;

import com.google.gson.JsonObject;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nightbeam.remnants.mobs.config.Json.arr;
import static com.nightbeam.remnants.mobs.config.Json.obj;

/** {@code config/remnant_mobs/traits.json}: which traits can roll, how often, and their numbers. */
public final class TraitsConfig {
    private final Map<Trait, Settings> traits = new EnumMap<>(Trait.class);

    /** enabled, roll weight, the lowest rank that can roll it, mobs that never get it, and the trait's own numbers. */
    public record Settings(boolean enabled, int weight, Rank minRank, List<String> exclude, JsonObject values) {
    }

    private TraitsConfig(JsonObject o) {
        for (Trait trait : Trait.values()) {
            JsonObject defaults = DEFAULTS.get(trait);
            JsonObject t = Json.object(o, trait.key());
            Rank minRank = Rank.byKey(Json.str(t, "min_rank", Json.str(defaults, "min_rank", "veteran")));
            traits.put(trait, new Settings(Json.bool(t, "enabled", true), Math.max(0, Json.integer(t, "weight", Json.integer(defaults, "weight", 10))),
                    minRank == null || minRank == Rank.COMMON ? Rank.VETERAN : minRank, Json.strings(t, "exclude"), t));
        }
    }

    public Settings settings(Trait trait) {
        return traits.get(trait);
    }

    /** A number of the trait, falling back to the built-in default. */
    public double value(Trait trait, String key) {
        double fallback = Json.num(DEFAULTS.get(trait), key, 0);
        return Json.num(traits.get(trait).values(), key, fallback);
    }

    public static TraitsConfig from(JsonObject json) {
        return new TraitsConfig(json);
    }

    private static final Map<Trait, JsonObject> DEFAULTS = new LinkedHashMap<>();

    private static void def(Trait trait, int weight, String minRank, String comment, Object... values) {
        JsonObject json = obj("enabled", true, "weight", weight, "min_rank", minRank, "exclude", arr());
        JsonObject extra = obj(values);
        extra.entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
        DEFAULTS.put(trait, json);
        COMMENTS.put(trait, comment);
    }

    private static final Map<Trait, String> COMMENTS = new EnumMap<>(Trait.class);

    static {
        def(Trait.VAMPIRIC, 10, "veteran", "Heals heal_fraction of the damage it deals.", "heal_fraction", 0.3);
        def(Trait.FROSTBITE, 10, "veteran", "Hits slow for slowness_seconds (amplifier slowness_level) and add freeze_ticks of frost.",
                "slowness_seconds", 3, "slowness_level", 2, "freeze_ticks", 100);
        def(Trait.INFERNAL, 10, "veteran", "Fire immune; hits set the target on fire for fire_seconds.", "fire_seconds", 4);
        def(Trait.VENOMOUS, 10, "veteran", "Hits poison for poison_seconds (Poison II from Champion rank).", "poison_seconds", 4);
        def(Trait.WITHERING, 8, "elite", "Hits wither for wither_seconds.", "wither_seconds", 3);
        def(Trait.BERSERK, 10, "veteran", "Below below_health of its health: +damage_bonus damage and +speed_bonus speed.",
                "below_health", 0.5, "damage_bonus", 0.4, "speed_bonus", 0.25);
        def(Trait.CRUSHING, 8, "veteran", "Hits knock back knockback blocks harder and disable shields for shield_seconds.",
                "knockback", 1.5, "shield_seconds", 3);
        def(Trait.JUGGERNAUT, 8, "elite", "Immune to knockback, +health_bonus health, +armor armor, -speed_penalty speed.",
                "health_bonus", 0.5, "armor", 4, "speed_penalty", 0.1);
        def(Trait.THORNSKIN, 10, "veteran", "Reflects reflect of the melee damage it takes.", "reflect", 0.25);
        def(Trait.WARDED, 8, "elite", "Absorption worth shield_fraction of its max health, regrown regrow_seconds after it was last hurt.",
                "shield_fraction", 0.25, "regrow_seconds", 6);
        def(Trait.REGENERATING, 10, "veteran", "Regains per_second of its max health each second, in_combat while hurt in the last combat_seconds.",
                "per_second", 0.02, "in_combat", 0.005, "combat_seconds", 3);
        def(Trait.DEFLECTING, 10, "veteran", "Projectiles deal reduction less damage to it.", "reduction", 0.6);
        def(Trait.SWIFT, 10, "veteran", "+speed_bonus movement speed.", "speed_bonus", 0.3);
        def(Trait.BLINKING, 8, "elite", "When hit, chance to teleport behind its attacker (within range blocks), every cooldown_seconds at most.",
                "chance", 0.3, "cooldown_seconds", 5, "range", 8);
        def(Trait.PACKLEADER, 6, "elite", "Below health_threshold of its health, once, calls minions copies minion_levels levels lower.",
                "health_threshold", 0.6, "minions", 2, "minion_levels", 5);
        def(Trait.VOLATILE, 6, "veteran", "Explodes fuse_seconds after death with power power (no block damage).",
                "power", 2.5, "fuse_seconds", 1.5);
    }

    public static CommentedJson spec() {
        JsonObject d = new JsonObject();
        DEFAULTS.forEach((trait, json) -> d.add(trait.key(), json.deepCopy()));
        CommentedJson spec = new CommentedJson(d)
                .header("Remnants: RPG Mobs 3.0: traits of ranked mobs.\n"
                        + "Every trait has enabled, weight (relative roll chance), min_rank (the lowest rank that can roll it)\n"
                        + "and exclude (entity ids, #entity_type tags or \"namespace:\" prefixes that never get it).\n"
                        + "Infernal and Frostbite never roll together, nor Swift and Juggernaut.\n"
                        + "Edit and run /remnants reload, or restart. Missing keys are restored with their defaults.");
        COMMENTS.forEach((trait, comment) -> spec.comment(trait.key(), comment));
        return spec;
    }
}
