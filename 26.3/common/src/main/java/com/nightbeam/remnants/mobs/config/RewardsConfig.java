package com.nightbeam.remnants.mobs.config;

import com.google.gson.JsonObject;
import com.nightbeam.remnants.mobs.content.Rank;
import java.util.EnumMap;
import java.util.Map;

import static com.nightbeam.remnants.mobs.config.Json.obj;

/** {@code config/remnant_mobs/rewards.json}: experience, loot, Remnant Shards and RPG Attribute System Valor. */
public final class RewardsConfig {
    public final double experiencePerLevel;
    public final boolean extraLoot;
    public final boolean shards;
    public final boolean requirePlayerKill;
    public final boolean valorEnabled;
    public final Map<Rank, Double> valorPerRank = new EnumMap<>(Rank.class);
    public final double valorPerLevel;
    public final double challengeMax;
    public final int challengeMaxAt;
    public final double challengeMin;
    public final int challengeMinAt;
    public final boolean valorMessage;
    public final double shardValor;
    public final int shardExperience;
    public final double brandedMultiplier;

    private RewardsConfig(JsonObject o) {
        experiencePerLevel = Math.max(0.0, Json.num(o, "experience.per_level", 0.04));
        extraLoot = Json.bool(o, "loot.extra_rolls", true);
        shards = Json.bool(o, "loot.shards", true);
        requirePlayerKill = Json.bool(o, "loot.require_player_kill", true);
        valorEnabled = Json.bool(o, "valor.enabled", true);
        for (Rank rank : Rank.values()) {
            valorPerRank.put(rank, Math.max(0.0, Json.num(o, "valor.per_rank." + rank.key(), DEFAULT_VALOR.getOrDefault(rank, 0.0))));
        }
        valorPerLevel = Math.max(0.0, Json.num(o, "valor.per_level", 1.0));
        challengeMax = Math.max(1.0, Json.num(o, "valor.challenge.max", 1.5));
        challengeMaxAt = Math.max(1, Json.integer(o, "valor.challenge.max_at", 10));
        challengeMin = Math.max(0.0, Math.min(1.0, Json.num(o, "valor.challenge.min", 0.25)));
        challengeMinAt = Math.max(1, Json.integer(o, "valor.challenge.min_at", 15));
        valorMessage = Json.bool(o, "valor.message", true);
        shardValor = Math.max(0.0, Json.num(o, "shards.valor", 15));
        shardExperience = Math.max(0, Json.integer(o, "shards.experience", 8));
        brandedMultiplier = Math.max(1.0, Json.num(o, "branded_multiplier", 2.0));
    }

    /**
     * How much fighting above (or below) your level is worth: rises linearly to {@link #challengeMax} at
     * {@link #challengeMaxAt} levels above you and falls to {@link #challengeMin} at {@link #challengeMinAt} below.
     */
    public double challenge(int levelDifference) {
        if (levelDifference >= 0) {
            return 1.0 + (challengeMax - 1.0) * Math.min(levelDifference, challengeMaxAt) / challengeMaxAt;
        }
        return 1.0 - (1.0 - challengeMin) * Math.min(-levelDifference, challengeMinAt) / challengeMinAt;
    }

    public static RewardsConfig from(JsonObject json) {
        return new RewardsConfig(json);
    }

    private static final Map<Rank, Double> DEFAULT_VALOR = Map.of(
            Rank.COMMON, 0.0, Rank.VETERAN, 10.0, Rank.ELITE, 25.0, Rank.CHAMPION, 60.0, Rank.REMNANT, 150.0);

    public static CommentedJson spec() {
        JsonObject perRank = new JsonObject();
        for (Rank rank : Rank.values()) {
            perRank.addProperty(rank.key(), DEFAULT_VALOR.get(rank));
        }
        JsonObject d = obj(
                "experience", obj("per_level", 0.04),
                "loot", obj("extra_rolls", true, "shards", true, "require_player_kill", true),
                "valor", obj("enabled", true, "per_rank", perRank, "per_level", 1.0,
                        "challenge", obj("max", 1.5, "max_at", 10, "min", 0.25, "min_at", 15), "message", true),
                "shards", obj("valor", 15, "experience", 8),
                "branded_multiplier", 2.0);
        return new CommentedJson(d)
                .header("Remnants: RPG Mobs 3.0: rewards.\nEdit and run /remnants reload, or restart. Missing keys are restored with their defaults.")
                .comment("experience", "Dropped experience is multiplied by (1 + level x per_level), then by the rank's experience.")
                .comment("loot", "extra_rolls: ranked mobs roll their loot table again (loot_rolls in ranks.json).\n"
                        + "shards: ranked mobs drop Remnant Shards. require_player_kill: only when a player took part in the kill.")
                .comment("valor", "With RPG Attribute System: bonus Valor for the player who kills a ranked mob, on top of RAS's own\n"
                        + "kill Valor. Bonus = (per_rank + level x per_level) x challenge. challenge rises to max when the mob\n"
                        + "is max_at levels above the player and falls to min at min_at levels below. message: show the bonus\n"
                        + "above the hotbar.")
                .comment("shards", "Absorbing a Remnant Shard grants valor Valor with RPG Attribute System, or experience points without it.")
                .comment("branded_multiplier", "Mobs marked with a Brand of Challenge multiply every reward above by this.");
    }
}
