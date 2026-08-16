package tn.nightbeam.rpgmoblevelingsystem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModConfigDimensionsTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @AfterEach
    void resetConfigRoot() {
        ModConfig.setConfigRootForTests(null);
    }

    @Test
    void roundTripsOverworldMinZero() {
        ModConfig.DimensionsSettings settings = new ModConfig.DimensionsSettings();
        settings.dimensions = new ArrayList<>(List.of(
                new ModConfig.DimensionRange("minecraft:overworld", 0, 40),
                new ModConfig.DimensionRange("minecraft:the_nether", 20, 80)
        ));
        String json = GSON.toJson(settings);
        ModConfig.DimensionsSettings loaded = GSON.fromJson(json, ModConfig.DimensionsSettings.class);
        assertNotNull(loaded);
        assertNotNull(loaded.dimensions);
        assertEquals(2, loaded.dimensions.size());
        assertEquals("minecraft:overworld", loaded.dimensions.get(0).dimensionId);
        assertEquals(0.0, loaded.dimensions.get(0).min, 0.0001);
        assertEquals(40.0, loaded.dimensions.get(0).max, 0.0001);
    }

    @Test
    void ensureDefaultsPreservesOverworldMinZeroAndCustomDims() throws Exception {
        Path temp = Files.createTempDirectory("rml-config-test");
        Path configDir = temp.resolve("config").resolve("rpgmoblevelingsystem");
        Files.createDirectories(configDir);
        String body = """
                {
                  "dimensions": [
                    { "dimensionId": "minecraft:overworld", "min": 0.0, "max": 40.0 },
                    { "dimensionId": "custom:dim", "min": 5.0, "max": 50.0 }
                  ]
                }
                """;
        Files.writeString(configDir.resolve("dimensions_settings.json"), body, StandardCharsets.UTF_8);
        // Stub sibling configs so ensureDefaults does not regenerate defaults that touch Minecraft classes.
        Files.writeString(configDir.resolve("attributes_settings.json"), "{\"attributes\":[]}", StandardCharsets.UTF_8);
        Files.writeString(configDir.resolve("global_settings.json"),
                "{\"ascendantLootTable\":\"minecraft:chests/end_city_treasure\",\"ascendantEffectsMinLevel\":50,\"ascendantEffectsChance\":0.5,\"randomEffects\":true,\"useLegacyHud\":true,\"hideHudFor\":[\"touhou_little_maid:\"],\"overlayXOffset\":0,\"overlayYOffset\":0.45,\"overlayZOffset\":0,\"nameFormat\":\"%mob_name% [Lv. %level%]\",\"jadeEnabled\":true,\"overlayDebug\":false}",
                StandardCharsets.UTF_8);
        Files.writeString(configDir.resolve("scale_settings.json"),
                "{\"scaleType\":\"both\",\"baseLevel\":1,\"scaleFactor\":0.2,\"scaleDistance\":2500,\"dayFactor\":1,\"xpModifier\":5,\"roundingMode\":\"floor\",\"playerScaleMode\":\"none\",\"playerScaleRadius\":64,\"playerScaleFactor\":0.5,\"playerBalanceEnabled\":true,\"playerBalanceHealthRatio\":0.15,\"playerBalanceDamageRatio\":0.1,\"motpSearchRadius\":128}",
                StandardCharsets.UTF_8);
        Files.writeString(configDir.resolve("mobs_list_settings.json"),
                "{\"banned\":[],\"bannedNamespaces\":[],\"hostileOnly\":false,\"deathMessages\":false,\"lockedMobs\":{},\"bossLevelLocks\":{},\"structureMinLevels\":{},\"canBeAscendant\":[]}",
                StandardCharsets.UTF_8);
        Files.writeString(configDir.resolve("loot_by_level.json"), "{\"enabled\":false,\"bands\":[]}", StandardCharsets.UTF_8);

        ModConfig.setConfigRootForTests(configDir);
        ModConfig.ensureDefaults();

        String after = Files.readString(configDir.resolve("dimensions_settings.json"), StandardCharsets.UTF_8);
        ModConfig.DimensionsSettings loaded = GSON.fromJson(after, ModConfig.DimensionsSettings.class);
        assertNotNull(loaded.dimensions);
        assertEquals(2, loaded.dimensions.size());
        assertEquals(0.0, loaded.dimensions.get(0).min, 0.0001);
        assertEquals("custom:dim", loaded.dimensions.get(1).dimensionId);
        assertEquals(5.0, loaded.dimensions.get(1).min, 0.0001);
    }
}
