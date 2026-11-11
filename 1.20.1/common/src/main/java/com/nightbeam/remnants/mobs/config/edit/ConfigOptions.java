package com.nightbeam.remnants.mobs.config.edit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Kind;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Side;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static com.nightbeam.remnants.mobs.config.edit.ConfigOption.ANY;

/**
 * The options the config screen shows, grouped into tabs. Ranges and allowed values are the ones the loaders in
 * {@code config/} clamp to (ConfigOptionsTest checks them against the loaders); where a loader has no limit (trait
 * numbers), the documented sensible range is used, which is stricter but never something the loader would change.
 * Map entries (dimensions, structures, attribute rules, events) come from the values being edited.
 */
public final class ConfigOptions {
    public static final List<String> GROUPS = List.of("difficulty", "levels", "scaling", "ranks", "traits", "rewards", "nights", "display");
    private static final List<String> RANKS = List.of("veteran", "elite", "champion", "remnant");
    private static final List<String> ALL_RANKS = List.of("common", "veteran", "elite", "champion", "remnant");
    private static final List<String> CURVES = List.of("linear", "sqrt", "log", "power");
    private static final List<String> FACTORS = List.of("days", "played", "distance", "depth", "gear");
    private static final Set<String> TRAIT_FRACTIONS = Set.of("vampiric.heal_fraction", "berserk.below_health",
            "juggernaut.speed_penalty", "regenerating.per_second", "regenerating.in_combat", "deflecting.reduction",
            "blinking.chance", "packleader.health_threshold");

    private ConfigOptions() {
    }

    /**
     * @param values   file name → its current values (map entries are read from here)
     * @param defaults file name → its default values, as a fresh install writes them
     */
    public static List<ConfigOption> all(Map<String, JsonObject> values, Map<String, JsonObject> defaults) {
        Builder b = new Builder(values, defaults);

        // Difficulty engine (difficulty.json): what makes the threat rise.
        b.in("difficulty", "difficulty.json");
        b.toggle("enabled");
        for (String factor : FACTORS) {
            String key = "factors." + factor;
            b.toggle(key + ".enabled", "factors.<factor>.enabled", factor);
            b.choice(key + ".curve", "factors.<factor>.curve", factor, CURVES);
            b.number(key + ".per", "factors.<factor>.per", factor, 1e-6, ANY);
            b.number(key + ".exponent", "factors.<factor>.exponent", factor, 0.01, ANY);
            b.number(key + ".max", "factors.<factor>.max", factor, -ANY, ANY);
            if (factor.equals("depth")) {
                b.integer("factors.depth.start_y", -ANY, ANY);
            }
        }
        b.number("gear_score.armor", 0, ANY);
        b.number("gear_score.toughness", 0, ANY);
        b.number("gear_score.attack_damage", 0, ANY);
        b.number("gear_score.extra_health", 0, ANY);
        b.number("rank_odds.max_multiplier", 1, ANY);
        b.choice("rank_odds.curve", CURVES);
        for (String dimension : b.keys("dimensions")) {
            b.number("dimensions." + dimension + ".multiplier", "dimensions.<dimension>.multiplier", dimension, 0, ANY);
            b.number("dimensions." + dimension + ".offset", "dimensions.<dimension>.offset", dimension, -ANY, ANY);
        }
        b.toggle("compat.disable_overlaps");

        // Levels (levels.json): bands, the 3.0 distance/time/depth numbers, spread, players, structures.
        b.in("levels", "levels.json");
        b.toggle("enabled");
        b.toggle("affect_passive");
        b.integer("spread", 0, ANY);
        b.choice("players.source", List.of("ras", "vanilla", "none"));
        b.choice("players.mode", List.of("average", "nearest", "highest"));
        b.number("players.radius", 1, ANY);
        b.number("players.weight", 0, 1);
        b.number("distance.blocks_per_level", 1, ANY);
        b.number("time.days_per_level", 0.01, ANY);
        b.number("time.max_levels", 0, ANY);
        b.integer("depth.start_y", -ANY, ANY);
        b.number("depth.blocks_per_level", 1, ANY);
        b.number("depth.max_levels", 0, ANY);
        b.integer("default_band.min", 0, ANY);
        b.integerAtLeast("default_band.max", "default_band.max", null, 0, "default_band.min");
        b.number("default_band.distance_scale", 0, ANY);
        for (String dimension : b.keys("dimensions")) {
            String key = "dimensions." + dimension;
            b.integer(key + ".min", "dimensions.<dimension>.min", dimension, 0, ANY);
            b.integerAtLeast(key + ".max", "dimensions.<dimension>.max", dimension, 0, key + ".min");
            b.number(key + ".distance_scale", "dimensions.<dimension>.distance_scale", dimension, 0, ANY);
        }
        for (String structure : b.keys("structures")) {
            b.integer("structures." + structure, "structures.<structure>", structure, 0, ANY);
        }

        // What a level does (levels.json).
        b.in("scaling", "levels.json");
        JsonElement rules = ConfigPaths.get(values.get("levels.json"), "attributes");
        if (rules instanceof JsonArray list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof JsonObject rule && rule.get("attribute") instanceof JsonPrimitive id && !id.getAsString().isBlank()) {
                    String key = "attributes[" + i + "]";
                    String attribute = id.getAsString();
                    JsonObject fallback = defaultRule(defaults.get("levels.json"), attribute);
                    b.add(key + ".mode", "attributes[].mode", attribute, Kind.CHOICE, 0, 0,
                            List.of("additive", "percent", "multiplicative"), null, fallback == null ? null : fallback.get("mode"));
                    b.add(key + ".per_level", "attributes[].per_level", attribute, Kind.NUMBER, -ANY, ANY, List.of(), null,
                            fallback == null ? null : fallback.get("per_level"));
                    b.add(key + ".max", "attributes[].max", attribute, Kind.NUMBER, -ANY, ANY, List.of(), null,
                            fallback == null ? null : fallback.get("max"));
                }
            }
        }
        b.toggle("scale_projectile_damage");
        b.toggle("mobs.boss_stats");

        // Ranks (ranks.json).
        b.in("ranks", "ranks.json");
        b.toggle("enabled");
        b.number("level_chance_divisor", 1, ANY);
        b.number("spawner_multiplier", 0, ANY);
        for (String rank : RANKS) {
            String key = "ranks." + rank;
            b.number(key + ".chance", "ranks.<rank>.chance", rank, 0, 1);
            b.integer(key + ".min_level", "ranks.<rank>.min_level", rank, 0, ANY);
            b.number(key + ".health", "ranks.<rank>.health", rank, 0.1, ANY);
            b.number(key + ".damage", "ranks.<rank>.damage", rank, 0.1, ANY);
            b.number(key + ".armor", "ranks.<rank>.armor", rank, -ANY, ANY);
            b.integer(key + ".traits", "ranks.<rank>.traits", rank, 0, 4);
            b.number(key + ".experience", "ranks.<rank>.experience", rank, 0, ANY);
            b.integer(key + ".loot_rolls", "ranks.<rank>.loot_rolls", rank, 0, ANY);
            b.integer(key + ".shards_min", "ranks.<rank>.shards_min", rank, 0, ANY);
            b.integerAtLeast(key + ".shards_max", "ranks.<rank>.shards_max", rank, 0, key + ".shards_min");
        }
        b.toggle("remnant.titles");
        b.toggle("remnant.boss_bar");
        b.toggle("remnant.announce");
        b.number("remnant.horn_radius", 0, ANY);

        // Traits (traits.json).
        b.in("traits", "traits.json");
        JsonObject traitDefaults = defaults.getOrDefault("traits.json", new JsonObject());
        for (Map.Entry<String, JsonElement> trait : traitDefaults.entrySet()) {
            String t = trait.getKey();
            b.toggle(t + ".enabled", "<trait>.enabled", t);
            b.integer(t + ".weight", "<trait>.weight", t, 0, ANY);
            b.choice(t + ".min_rank", "<trait>.min_rank", t, RANKS);
            if (trait.getValue() instanceof JsonObject numbers) {
                for (String key : numbers.keySet()) {
                    if (Set.of("enabled", "weight", "min_rank", "exclude").contains(key)) {
                        continue;
                    }
                    String path = t + "." + key;
                    if (TRAIT_FRACTIONS.contains(path)) {
                        b.number(path, path, t, 0, 1);
                    } else if (path.equals("frostbite.slowness_level")) {
                        b.integer(path, path, t, 1, 10);
                    } else if (path.equals("frostbite.freeze_ticks") || path.equals("packleader.minion_levels")) {
                        b.integer(path, path, t, 0, ANY);
                    } else if (path.equals("packleader.minions")) {
                        b.integer(path, path, t, 0, 16);
                    } else if (path.equals("juggernaut.armor")) {
                        b.number(path, path, t, -ANY, ANY);
                    } else {
                        b.number(path, path, t, 0, ANY);
                    }
                }
            }
        }

        // Rewards (rewards.json, loot.json scaling, progression.json drops).
        b.in("rewards", "rewards.json");
        b.number("experience.per_level", 0, ANY);
        b.toggle("loot.extra_rolls");
        b.toggle("loot.shards");
        b.toggle("loot.require_player_kill");
        b.toggle("valor.enabled");
        for (String rank : ALL_RANKS) {
            b.number("valor.per_rank." + rank, "valor.per_rank.<rank>", rank, 0, ANY);
        }
        b.number("valor.per_level", 0, ANY);
        b.number("valor.challenge.max", 1, ANY);
        b.integer("valor.challenge.max_at", 1, ANY);
        b.number("valor.challenge.min", 0, 1);
        b.integer("valor.challenge.min_at", 1, ANY);
        b.toggle("valor.message");
        b.number("shards.valor", 0, ANY);
        b.integer("shards.experience", 0, ANY);
        b.number("branded_multiplier", 1, ANY);
        b.in("rewards", "loot.json");
        b.toggle("enabled");
        b.number("scaling.extra_rolls_bonus", 0, ANY);
        b.number("scaling.shards_bonus", 0, ANY);
        b.number("scaling.common_extra_roll", 0, 1);
        b.in("rewards", "progression.json");
        for (String rank : RANKS) {
            b.number("drops.vital_ember." + rank, "drops.vital_ember.<rank>", rank, 0, 1);
        }
        b.number("drops.vital_ember.event_boss", 0, 1);
        b.number("drops.mending_salve", 0, 1);
        b.number("drops.hunters_draught", 0, 1);
        b.number("drops.difficulty_bonus", 0, ANY);

        // Dangerous nights (levels.json Hunter's Moon, events.json).
        b.in("nights", "levels.json");
        b.toggle("hunters_moon.enabled");
        b.number("hunters_moon.chance", 0, 1);
        b.integer("hunters_moon.level_bonus", 0, ANY);
        b.number("hunters_moon.rank_chance_multiplier", 0, ANY);
        b.number("hunters_moon.shard_multiplier", 0, ANY);
        b.in("nights", "events.json");
        b.toggle("enabled");
        for (String event : b.keys("events")) {
            String key = "events." + event;
            b.toggle(key + ".enabled", "events.<event>.enabled", event);
            b.integer(key + ".trigger.every_days", "events.<event>.trigger.every_days", event, 1, ANY);
            b.integer(key + ".trigger.first_day", "events.<event>.trigger.first_day", event, 0, ANY);
            b.number(key + ".trigger.chance", "events.<event>.trigger.chance", event, 0, 1);
            b.number(key + ".trigger.min_difficulty", "events.<event>.trigger.min_difficulty", event, 0, 1);
            b.choice(key + ".trigger.moon", "events.<event>.trigger.moon", event, List.of("any", "full", "new"));
            b.integer(key + ".effects.level_bonus", "events.<event>.effects.level_bonus", event, 0, ANY);
            b.number(key + ".effects.rank_multiplier", "events.<event>.effects.rank_multiplier", event, 0, ANY);
            b.number(key + ".effects.shard_multiplier", "events.<event>.effects.shard_multiplier", event, 0, ANY);
            b.integer(key + ".hunters.waves", "events.<event>.hunters.waves", event, 0, 20);
            b.integer(key + ".hunters.per_wave", "events.<event>.hunters.per_wave", event, 0, 16);
            b.integer(key + ".hunters.max_alive", "events.<event>.hunters.max_alive", event, 1, ANY);
        }

        // What you see: the difficulty meter, nameplates and HUD. Mostly read by each player's own game.
        b.in("display", "progression.json");
        b.side = Side.CLIENT;
        b.toggle("meter.hud");
        b.choice("meter.anchor", List.of("top_left", "top_right", "bottom_left", "bottom_right"));
        b.integer("meter.x", -ANY, ANY);
        b.integer("meter.y", -ANY, ANY);
        b.side = Side.SERVER;
        b.toggle("enabled");
        b.toggle("meter.send_to_players");
        b.in("display", "client.json");
        b.side = Side.CLIENT;
        b.toggle("nameplates.enabled");
        b.number("nameplates.distance", 2, 64);
        b.number("nameplates.common_distance", 0, 64);
        b.number("nameplates.traits_distance", 0, 64);
        b.toggle("nameplates.look_at_only");
        b.toggle("nameplates.health_numbers");
        b.number("nameplates.scale", 0.5, 2);
        b.choice("nameplates.level_colors", List.of("relative", "absolute"));
        b.toggle("nameplates.replace_name_tag");
        b.toggle("nameplates.hide_with_hud");
        b.toggle("hud.lens");
        b.toggle("hud.hunters_moon");
        return b.out;
    }

    /** The option for {@code file} and {@code path}, or null when the screen does not edit it. */
    public static @Nullable ConfigOption find(List<ConfigOption> options, String file, String path) {
        for (ConfigOption option : options) {
            if (option.file().equals(file) && option.path().equals(path)) {
                return option;
            }
        }
        return null;
    }

    private static @Nullable JsonObject defaultRule(@Nullable JsonObject levelsDefaults, String attribute) {
        if (ConfigPaths.get(levelsDefaults, "attributes") instanceof JsonArray list) {
            String wanted = normalizeAttribute(attribute);
            for (JsonElement element : list) {
                if (element instanceof JsonObject rule && rule.get("attribute") instanceof JsonPrimitive id
                        && normalizeAttribute(id.getAsString()).equals(wanted)) {
                    return rule;
                }
            }
        }
        return null;
    }

    /** {@code max_health}, {@code minecraft:max_health} and {@code minecraft:generic.max_health} name one rule. */
    static String normalizeAttribute(String id) {
        String value = id.trim().toLowerCase(java.util.Locale.ROOT);
        if (!value.contains(":")) {
            value = "minecraft:" + value;
        }
        int colon = value.indexOf(':');
        String path = value.substring(colon + 1);
        for (String prefix : new String[] {"generic.", "player.", "zombie."}) {
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }
        }
        return value.substring(0, colon) + ":" + path;
    }

    private static final class Builder {
        final List<ConfigOption> out = new ArrayList<>();
        final Map<String, JsonObject> values;
        final Map<String, JsonObject> defaults;
        String group = "";
        String file = "";
        Side side = Side.SERVER;

        Builder(Map<String, JsonObject> values, Map<String, JsonObject> defaults) {
            this.values = values;
            this.defaults = defaults;
        }

        void in(String group, String file) {
            this.group = group;
            this.file = file;
            this.side = Side.SERVER;
        }

        /** Keys of an object in the current file, editable ones only, objects as values when {@code objects}. */
        List<String> keys(String path) {
            List<String> keys = new ArrayList<>();
            if (ConfigPaths.get(values.get(file), path) instanceof JsonObject object) {
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    if (ConfigPaths.plain(entry.getKey())) {
                        keys.add(entry.getKey());
                    }
                }
            }
            return keys;
        }

        void toggle(String path) {
            toggle(path, path, null);
        }

        void toggle(String path, String docKey, @Nullable String entry) {
            add(path, docKey, entry, Kind.TOGGLE, 0, 0, List.of(), null, fallback(path, docKey));
        }

        void choice(String path, List<String> choices) {
            choice(path, path, null, choices);
        }

        void choice(String path, String docKey, @Nullable String entry, List<String> choices) {
            add(path, docKey, entry, Kind.CHOICE, 0, 0, choices, null, fallback(path, docKey));
        }

        void number(String path, double min, double max) {
            number(path, path, null, min, max);
        }

        void number(String path, String docKey, @Nullable String entry, double min, double max) {
            add(path, docKey, entry, Kind.NUMBER, min, max, List.of(), null, fallback(path, docKey));
        }

        void integer(String path, double min, double max) {
            integer(path, path, null, min, max);
        }

        void integer(String path, String docKey, @Nullable String entry, double min, double max) {
            add(path, docKey, entry, Kind.INTEGER, min, max, List.of(), null, fallback(path, docKey));
        }

        void integerAtLeast(String path, String docKey, @Nullable String entry, double min, String atLeast) {
            add(path, docKey, entry, Kind.INTEGER, min, ANY, List.of(), atLeast, fallback(path, docKey));
        }

        void add(String path, String docKey, @Nullable String entry, Kind kind, double min, double max, List<String> choices,
                 @Nullable String atLeast, @Nullable JsonElement defaultValue) {
            out.add(new ConfigOption(group, file, path, docKey, entry, kind, min, max, choices, side, atLeast, defaultValue));
        }

        /**
         * The default at {@code path}; for map entries a fresh install does not have, what the loader falls back to:
         * a dimension's band keys come from default_band, an event's keys from the built-in Grim Vigil, a dimension
         * multiplier is 1 and an offset 0.
         */
        @Nullable JsonElement fallback(String path, String docKey) {
            JsonObject fileDefaults = defaults.get(file);
            JsonElement value = ConfigPaths.get(fileDefaults, path);
            if (value != null) {
                return value;
            }
            String last = path.substring(path.lastIndexOf('.') + 1);
            if (file.equals("levels.json") && docKey.startsWith("dimensions.<dimension>.")) {
                JsonElement band = ConfigPaths.get(values.get(file), "default_band." + last);
                return band != null ? band : ConfigPaths.get(fileDefaults, "default_band." + last);
            }
            if (file.equals("difficulty.json") && docKey.equals("dimensions.<dimension>.multiplier")) {
                return new JsonPrimitive(1);
            }
            if (file.equals("difficulty.json") && docKey.equals("dimensions.<dimension>.offset")) {
                return new JsonPrimitive(0);
            }
            if (file.equals("events.json") && docKey.startsWith("events.<event>.")) {
                return ConfigPaths.get(fileDefaults, "events.grim_vigil." + docKey.substring("events.<event>.".length()));
            }
            return null;
        }
    }
}
