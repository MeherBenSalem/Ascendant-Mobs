package com.nightbeam.remnants.mobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nightbeam.remnants.mobs.config.CommentedJson;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.RanksConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.RewardsConfig;
import com.nightbeam.remnants.mobs.config.TraitsConfig;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigTest {
    @Test
    void defaultsMatchTheDesign() {
        LevelsConfig levels = LevelsConfig.from(LevelsConfig.spec().defaults());
        assertEquals(1, levels.band("minecraft:overworld").min());
        assertEquals(80, levels.band("minecraft:the_nether").max());
        assertEquals(35, levels.band("minecraft:the_end").min());
        assertEquals(40, levels.structures.get("minecraft:ancient_city"));
        assertEquals(7, levels.attributes.size());
        RanksConfig ranks = RanksConfig.from(RanksConfig.spec().defaults());
        assertEquals(2.0, ranks.stats(Rank.ELITE).health());
        assertEquals(4, ranks.stats(Rank.REMNANT).traits());
        TraitsConfig traits = TraitsConfig.from(TraitsConfig.spec().defaults());
        assertEquals(0.3, traits.value(Trait.VAMPIRIC, "heal_fraction"));
        assertEquals(Rank.ELITE, traits.settings(Trait.WITHERING).minRank());
    }

    @Test
    void challengeCurve() {
        RewardsConfig rewards = RewardsConfig.from(RewardsConfig.spec().defaults());
        assertEquals(1.0, rewards.challenge(0), 1e-9);
        assertEquals(1.5, rewards.challenge(10), 1e-9);
        assertEquals(1.5, rewards.challenge(40), 1e-9);
        assertEquals(1.25, rewards.challenge(5), 1e-9);
        assertEquals(0.25, rewards.challenge(-15), 1e-9);
        assertEquals(0.25, rewards.challenge(-100), 1e-9);
    }

    @Test
    void missingKeysComeBack(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("ranks.json");
        Files.writeString(file, "{ // edited\n \"ranks\": { \"elite\": { \"chance\": 0.5 } } }", StandardCharsets.UTF_8);
        CommentedJson spec = RanksConfig.spec();
        RanksConfig ranks = RanksConfig.from(spec.load(file));
        assertEquals(0.5, ranks.stats(Rank.ELITE).chance());
        assertEquals(8, ranks.stats(Rank.ELITE).minLevel());
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("\"shards_max\""));
    }

    @Test
    void migratesCustomised2xConfig(@TempDir Path root) throws IOException {
        Path legacy = Files.createDirectories(root.resolve("rpgmoblevelingsystem"));
        Files.writeString(legacy.resolve("dimensions_settings.json"),
                "{\"dimensions\":[{\"dimensionId\":\"minecraft:overworld\",\"min\":5,\"max\":90}]}", StandardCharsets.UTF_8);
        Files.writeString(legacy.resolve("scale_settings.json"), "{\"scaleDistance\":1000,\"scaleFactor\":1.0,\"playerScaleMode\":\"nearest\"}",
                StandardCharsets.UTF_8);
        Files.writeString(legacy.resolve("mobs_list_settings.json"),
                "{\"banned\":[\"minecraft:bat\",\"minecraft:creeper\"],\"bannedNamespaces\":[\"cobblemon\",\"alexsmobs\"],"
                        + "\"lockedMobs\":{\"minecraft:wither\":10.0,\"minecraft:husk\":30.0},\"structureMinLevels\":{\"minecraft:igloo\":9}}",
                StandardCharsets.UTF_8);
        RemnantsConfig.load(root);
        LevelsConfig levels = RemnantsConfig.levels();
        assertEquals(5, levels.band("minecraft:overworld").min());
        assertEquals(90, levels.band("minecraft:overworld").max());
        assertEquals(1000, levels.blocksPerLevel);
        assertEquals("nearest", levels.playerMode);
        assertTrue(levels.exclude.contains("minecraft:creeper"));
        assertTrue(levels.exclude.contains("alexsmobs:"));
        assertTrue(!levels.exclude.contains("minecraft:bat"));
        assertEquals(30, levels.levelLocks.get("minecraft:husk"));
        assertTrue(!levels.levelLocks.containsKey("minecraft:wither"));
        assertEquals(9, levels.structures.get("minecraft:igloo"));
        assertTrue(Files.exists(root.resolve("remnant_mobs").resolve("traits.json")));
    }

    @Test
    void untouched2xConfigIsNotMigrated(@TempDir Path root) throws IOException {
        Path legacy = Files.createDirectories(root.resolve("rpgmoblevelingsystem"));
        Files.writeString(legacy.resolve("scale_settings.json"), "{\"scaleDistance\":2500,\"scaleFactor\":0.2,\"playerScaleMode\":\"none\"}",
                StandardCharsets.UTF_8);
        RemnantsConfig.load(root);
        assertEquals(128, RemnantsConfig.levels().blocksPerLevel);
    }
}
