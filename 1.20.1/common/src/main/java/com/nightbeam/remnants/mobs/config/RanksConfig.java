package com.nightbeam.remnants.mobs.config;

import com.google.gson.JsonObject;
import com.nightbeam.remnants.mobs.content.Rank;
import java.util.EnumMap;
import java.util.Map;

import static com.nightbeam.remnants.mobs.config.Json.obj;

/** {@code config/remnant_mobs/ranks.json}: how often each rank appears and what it gives a mob. */
public final class RanksConfig {
    public final boolean enabled;
    public final double levelDivisor;
    public final double spawnerMultiplier;
    public final Map<Rank, Stats> ranks = new EnumMap<>(Rank.class);
    public final boolean remnantTitles;
    public final boolean remnantBossBar;
    public final boolean remnantAnnounce;
    public final double remnantHornRadius;

    /** One rank's numbers. Common uses multipliers of 1 and no traits. */
    public record Stats(double chance, int minLevel, double health, double damage, double armor, int traits,
                        double experience, int lootRolls, int shardsMin, int shardsMax) {
    }

    private RanksConfig(JsonObject o) {
        enabled = Json.bool(o, "enabled", true);
        levelDivisor = Math.max(1.0, Json.num(o, "level_chance_divisor", 50));
        spawnerMultiplier = Math.max(0.0, Json.num(o, "spawner_multiplier", 0.25));
        ranks.put(Rank.COMMON, new Stats(0, 0, 1, 1, 0, 0, 1, 0, 0, 0));
        for (Rank rank : Rank.values()) {
            if (rank == Rank.COMMON) {
                continue;
            }
            Stats d = DEFAULTS.get(rank);
            JsonObject r = Json.object(o, "ranks." + rank.key());
            int shardsMin = Math.max(0, Json.integer(r, "shards_min", d.shardsMin()));
            ranks.put(rank, new Stats(
                    Math.max(0.0, Math.min(1.0, Json.num(r, "chance", d.chance()))),
                    Math.max(0, Json.integer(r, "min_level", d.minLevel())),
                    Math.max(0.1, Json.num(r, "health", d.health())),
                    Math.max(0.1, Json.num(r, "damage", d.damage())),
                    Json.num(r, "armor", d.armor()),
                    Math.max(0, Math.min(4, Json.integer(r, "traits", d.traits()))),
                    Math.max(0.0, Json.num(r, "experience", d.experience())),
                    Math.max(0, Json.integer(r, "loot_rolls", d.lootRolls())),
                    shardsMin,
                    Math.max(shardsMin, Json.integer(r, "shards_max", d.shardsMax()))));
        }
        remnantTitles = Json.bool(o, "remnant.titles", true);
        remnantBossBar = Json.bool(o, "remnant.boss_bar", true);
        remnantAnnounce = Json.bool(o, "remnant.announce", true);
        remnantHornRadius = Math.max(0.0, Json.num(o, "remnant.horn_radius", 48));
    }

    public Stats stats(Rank rank) {
        return ranks.get(rank);
    }

    public static RanksConfig from(JsonObject json) {
        return new RanksConfig(json);
    }

    private static final Map<Rank, Stats> DEFAULTS = new EnumMap<>(Map.of(
            Rank.VETERAN, new Stats(0.06, 1, 1.4, 1.1, 2, 1, 2, 0, 1, 1),
            Rank.ELITE, new Stats(0.015, 8, 2.0, 1.25, 4, 2, 3, 1, 2, 3),
            Rank.CHAMPION, new Stats(0.003, 15, 3.2, 1.45, 6, 3, 5, 2, 4, 6),
            Rank.REMNANT, new Stats(0.0003, 30, 5.0, 1.7, 10, 4, 10, 3, 8, 12)));

    private static JsonObject statsJson(Stats s) {
        return obj("chance", s.chance(), "min_level", s.minLevel(), "health", s.health(), "damage", s.damage(),
                "armor", s.armor(), "traits", s.traits(), "experience", s.experience(), "loot_rolls", s.lootRolls(),
                "shards_min", s.shardsMin(), "shards_max", s.shardsMax());
    }

    public static CommentedJson spec() {
        JsonObject ranks = new JsonObject();
        DEFAULTS.forEach((rank, stats) -> ranks.add(rank.key(), statsJson(stats)));
        JsonObject d = obj(
                "enabled", true,
                "level_chance_divisor", 50,
                "spawner_multiplier", 0.25,
                "ranks", ranks,
                "remnant", obj("titles", true, "boss_bar", true, "announce", true, "horn_radius", 48));
        return new CommentedJson(d)
                .header("Remnants: RPG Mobs 3.0: ranks.\nEdit and run /remnants reload, or restart. Missing keys are restored with their defaults.")
                .comment("enabled", "When false, every leveled mob is Common: no ranks, no traits.")
                .comment("level_chance_divisor", "Rank chances grow with level: chance x (1 + level / level_chance_divisor).")
                .comment("spawner_multiplier", "Rank chances of mobs from monster spawners are multiplied by this.")
                .comment("ranks", "Checked from the rarest rank down. chance: base chance (0-1). min_level: lowest level it\n"
                        + "can appear at. health and damage multiply the leveled stats; armor is added. traits: how many it\n"
                        + "rolls (0-4). experience multiplies dropped experience; loot_rolls rolls the mob's loot table that many\n"
                        + "extra times; it drops shards_min to shards_max Remnant Shards.")
                .comment("remnant", "Remnant-rank mobs: a generated title, a boss bar for nearby players, a server message when\n"
                        + "slain, and a horn heard by players within horn_radius blocks when one appears.");
    }
}
