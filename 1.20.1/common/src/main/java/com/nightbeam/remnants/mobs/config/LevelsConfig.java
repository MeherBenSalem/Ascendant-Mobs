package com.nightbeam.remnants.mobs.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.nightbeam.remnants.mobs.config.Json.arr;
import static com.nightbeam.remnants.mobs.config.Json.obj;

/** {@code config/remnant_mobs/levels.json}: how levels are worked out and what they do to a mob. */
public final class LevelsConfig {
    public final boolean enabled;
    public final boolean affectPassive;
    public final Band defaultBand;
    public final Map<String, Band> bands;
    public final double blocksPerLevel;
    public final double daysPerLevel;
    public final double maxDayLevels;
    public final int depthStartY;
    public final double blocksPerDepthLevel;
    public final double maxDepthLevels;
    public final int spread;
    public final Map<String, Integer> structures;
    public final String playerSource;
    public final String playerMode;
    public final double playerRadius;
    public final double playerWeight;
    public final List<AttributeRule> attributes;
    public final boolean scaleProjectiles;
    public final List<String> exclude;
    public final Map<String, Integer> levelLocks;
    public final boolean bossStats;
    public final boolean moonEnabled;
    public final double moonChance;
    public final int moonLevelBonus;
    public final double moonRankMultiplier;
    public final double moonShardMultiplier;

    /** The level range of a dimension. Distance levels are multiplied by distanceScale. */
    public record Band(int min, int max, double distanceScale) {
    }

    /**
     * Raises one attribute with level. additive: +perLevel per level. percent: +perLevel% of the base value per level.
     * multiplicative: compounds perLevel% per level. Each is capped at max (a value for additive, a percentage otherwise).
     */
    public record AttributeRule(String attribute, String mode, double perLevel, double max) {
    }

    private LevelsConfig(JsonObject o) {
        enabled = Json.bool(o, "enabled", true);
        affectPassive = Json.bool(o, "affect_passive", false);
        defaultBand = band(Json.object(o, "default_band"), new Band(1, 60, 1.0));
        bands = new LinkedHashMap<>();
        Json.object(o, "dimensions").entrySet().forEach(entry -> {
            if (entry.getValue().isJsonObject()) {
                bands.put(entry.getKey(), band(entry.getValue().getAsJsonObject(), defaultBand));
            }
        });
        blocksPerLevel = Math.max(1.0, Json.num(o, "distance.blocks_per_level", 128));
        daysPerLevel = Math.max(0.01, Json.num(o, "time.days_per_level", 4));
        maxDayLevels = Math.max(0.0, Json.num(o, "time.max_levels", 15));
        depthStartY = Json.integer(o, "depth.start_y", 32);
        blocksPerDepthLevel = Math.max(1.0, Json.num(o, "depth.blocks_per_level", 12));
        maxDepthLevels = Math.max(0.0, Json.num(o, "depth.max_levels", 8));
        spread = Math.max(0, Json.integer(o, "spread", 2));
        structures = new LinkedHashMap<>();
        Json.numbers(o, "structures").forEach((id, level) -> structures.put(id, level.intValue()));
        playerSource = Json.str(o, "players.source", "ras").toLowerCase(Locale.ROOT);
        playerMode = Json.str(o, "players.mode", "average").toLowerCase(Locale.ROOT);
        playerRadius = Math.max(1.0, Json.num(o, "players.radius", 64));
        playerWeight = Math.max(0.0, Math.min(1.0, Json.num(o, "players.weight", 0.35)));
        attributes = new ArrayList<>();
        JsonElement rules = Json.at(o, "attributes");
        if (rules != null && rules.isJsonArray()) {
            for (JsonElement element : rules.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject rule = element.getAsJsonObject();
                    String attribute = Json.str(rule, "attribute", "");
                    if (!attribute.isBlank()) {
                        attributes.add(new AttributeRule(attribute, Json.str(rule, "mode", "percent").toLowerCase(Locale.ROOT),
                                Json.num(rule, "per_level", 0), Json.num(rule, "max", 0)));
                    }
                }
            }
        }
        scaleProjectiles = Json.bool(o, "scale_projectile_damage", true);
        exclude = Json.strings(o, "mobs.exclude");
        levelLocks = new LinkedHashMap<>();
        Json.numbers(o, "mobs.level_locks").forEach((id, level) -> levelLocks.put(id, level.intValue()));
        bossStats = Json.bool(o, "mobs.boss_stats", false);
        moonEnabled = Json.bool(o, "hunters_moon.enabled", true);
        moonChance = Math.max(0.0, Math.min(1.0, Json.num(o, "hunters_moon.chance", 0.35)));
        moonLevelBonus = Math.max(0, Json.integer(o, "hunters_moon.level_bonus", 5));
        moonRankMultiplier = Math.max(0.0, Json.num(o, "hunters_moon.rank_chance_multiplier", 2.0));
        moonShardMultiplier = Math.max(0.0, Json.num(o, "hunters_moon.shard_multiplier", 2.0));
    }

    private static Band band(JsonObject o, Band fallback) {
        int min = Json.integer(o, "min", fallback.min());
        int max = Math.max(min, Json.integer(o, "max", fallback.max()));
        return new Band(Math.max(0, min), max, Math.max(0.0, Json.num(o, "distance_scale", fallback.distanceScale())));
    }

    public Band band(String dimension) {
        return bands.getOrDefault(dimension, defaultBand);
    }

    public static LevelsConfig from(JsonObject json) {
        return new LevelsConfig(json);
    }

    static JsonObject bandJson(int min, int max, double scale) {
        return obj("min", min, "max", max, "distance_scale", scale);
    }

    static JsonObject rule(String attribute, String mode, double perLevel, double max) {
        return obj("attribute", attribute, "mode", mode, "per_level", perLevel, "max", max);
    }

    public static JsonArray defaultAttributes() {
        return arr(
                rule("minecraft:max_health", "percent", 6.0, 600),
                rule("minecraft:attack_damage", "percent", 3.0, 300),
                rule("minecraft:armor", "additive", 0.15, 15),
                rule("minecraft:armor_toughness", "additive", 0.05, 6),
                rule("minecraft:movement_speed", "percent", 0.3, 30),
                rule("minecraft:knockback_resistance", "additive", 0.005, 0.5),
                rule("minecraft:follow_range", "percent", 0.5, 50));
    }

    public static CommentedJson spec() {
        JsonObject d = obj(
                "enabled", true,
                "affect_passive", false,
                "default_band", bandJson(1, 60, 1.0),
                "dimensions", obj(
                        "minecraft:overworld", bandJson(1, 60, 1.0),
                        "minecraft:the_nether", bandJson(15, 80, 1.0),
                        "minecraft:the_end", bandJson(35, 100, 1.0),
                        "remnant_bosses:eclipsed_realm", bandJson(30, 100, 1.0)),
                "distance", obj("blocks_per_level", 128),
                "time", obj("days_per_level", 4, "max_levels", 15),
                "depth", obj("start_y", 32, "blocks_per_level", 12, "max_levels", 8),
                "spread", 2,
                "structures", obj(
                        "minecraft:ancient_city", 40, "minecraft:end_city", 50, "minecraft:bastion_remnant", 30,
                        "minecraft:mansion", 30, "minecraft:stronghold", 25, "minecraft:monument", 25,
                        "minecraft:fortress", 20, "minecraft:trial_chambers", 20, "minecraft:pillager_outpost", 12),
                "players", obj("source", "ras", "mode", "average", "radius", 64, "weight", 0.35),
                "attributes", defaultAttributes(),
                "scale_projectile_damage", true,
                "mobs", obj("exclude", arr(), "level_locks", obj(), "boss_stats", false),
                "hunters_moon", obj("enabled", true, "chance", 0.35, "level_bonus", 5,
                        "rank_chance_multiplier", 2.0, "shard_multiplier", 2.0));
        return new CommentedJson(d)
                .header("Remnants: RPG Mobs 3.0: mob levels.\nEdit and run /remnants reload, or restart. Missing keys are restored with their defaults.")
                .comment("enabled", "Master switch. When false, new mobs get no level, rank or traits.")
                .comment("affect_passive", "Also level passive and neutral creatures (they never get ranks).")
                .comment("default_band", "Level range for dimensions not listed under dimensions.")
                .comment("dimensions", "Level range per dimension, and distance_scale: how much faster (above 1) or slower distance\n"
                        + "raises the threat there.")
                .comment("distance", "Threat grows by one level every blocks_per_level blocks (horizontal) from world spawn. Distance\n"
                        + "is measured in Overworld blocks, so one Nether block counts as eight.")
                .comment("time", "...and by one level every days_per_level in-game days, up to max_levels.")
                .comment("depth", "...and by one level every blocks_per_level blocks below start_y, up to max_levels.")
                .comment("spread", "Each mob's level varies by up to this much around the threat of its spawn point.")
                .comment("structures", "Minimum level of mobs spawning inside these structures.")
                .comment("players", "Player progression. source: \"ras\" (RPG Attribute System level), \"vanilla\" (experience level)\n"
                        + "or \"none\". mode: \"average\", \"nearest\" or \"highest\" of the players within radius blocks.\n"
                        + "weight 0 ignores players; 1 makes mobs match the players' level; 0.35 pulls the threat a third\n"
                        + "of the way toward it.")
                .comment("attributes", "What a level does. mode \"additive\": +per_level per level, up to max.\n"
                        + "\"percent\": +per_level% of the mob's base value per level, up to max%.\n"
                        + "\"multiplicative\": compounds per_level% per level, up to max%.")
                .comment("scale_projectile_damage", "Arrows, tridents and fireballs of leveled mobs scale like attack_damage.")
                .comment("mobs", "exclude: entity ids, #entity_type tags or \"namespace:\" prefixes that never get levels.\n"
                        + "level_locks: entity id -> fixed level. boss_stats: bosses (#remnant_mobs:bosses) get a level shown\n"
                        + "on their plate; set true to also scale their stats.")
                .comment("hunters_moon", "On full-moon nights, chance that a Hunter's Moon rises: mobs spawn level_bonus levels\n"
                        + "higher, ranks are rank_chance_multiplier times likelier and shards drop shard_multiplier times as\n"
                        + "often, until dawn.");
    }
}
