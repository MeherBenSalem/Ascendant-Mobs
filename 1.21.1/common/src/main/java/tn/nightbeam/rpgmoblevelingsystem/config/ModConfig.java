package tn.nightbeam.rpgmoblevelingsystem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import tn.nightbeam.rpgmoblevelingsystem.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_ROOT = Path.of("config", Constants.MOD_ID);

    private static GlobalSettings globalSettings = new GlobalSettings();
    private static ScaleSettings scaleSettings = new ScaleSettings();
    private static DimensionsSettings dimensionsSettings = new DimensionsSettings();
    private static AttributesSettings attributesSettings = new AttributesSettings();
    private static MobsListSettings mobsListSettings = new MobsListSettings();

    private ModConfig() {
    }

    public static synchronized void ensureDefaults() {
        try {
            Files.createDirectories(CONFIG_ROOT);
            globalSettings = readOrCreate("global_settings.json", GlobalSettings.class, defaultsGlobal());
            scaleSettings = readOrCreate("scale_settings.json", ScaleSettings.class, defaultsScale());
            dimensionsSettings = readOrCreate("dimensions_settings.json", DimensionsSettings.class, defaultsDimensions());
            attributesSettings = readOrCreate("attributes_settings.json", AttributesSettings.class, defaultsAttributes());
            mobsListSettings = readOrCreate("mobs_list_settings.json", MobsListSettings.class, defaultsMobsList());
            normalizeAndPersist();
        } catch (IOException e) {
            Constants.LOG.error("Failed to initialize config files", e);
        }
    }

    private static void normalizeAndPersist() throws IOException {
        boolean changedScale = false;
        boolean changedDimensions = false;

        if (scaleSettings.scaleDistance <= 0) {
            scaleSettings.scaleDistance = 2500;
            changedScale = true;
        }
        if (scaleSettings.scaleFactor <= 0) {
            scaleSettings.scaleFactor = 0.2;
            changedScale = true;
        }
        if (scaleSettings.dayFactor <= 0) {
            scaleSettings.dayFactor = 1;
            changedScale = true;
        }

        // Migrate legacy near-zero progression defaults to visible values.
        if (scaleSettings.baseLevel == 0 && scaleSettings.scaleFactor == 0.05 && scaleSettings.scaleDistance >= 100000) {
            scaleSettings.baseLevel = 1;
            scaleSettings.scaleFactor = 0.2;
            scaleSettings.scaleDistance = 2500;
            changedScale = true;
        }

        if (dimensionsSettings.dimensions != null) {
            for (DimensionRange range : dimensionsSettings.dimensions) {
                if ("minecraft:overworld".equals(range.dimensionId) && range.min <= 0) {
                    range.min = 1;
                    changedDimensions = true;
                }
            }
        }

        if (changedScale) {
            write(CONFIG_ROOT.resolve("scale_settings.json"), scaleSettings);
        }
        if (changedDimensions) {
            write(CONFIG_ROOT.resolve("dimensions_settings.json"), dimensionsSettings);
        }
    }

    private static <T> T readOrCreate(String fileName, Class<T> type, T defaults) throws IOException {
        Path path = CONFIG_ROOT.resolve(fileName);
        if (!Files.exists(path)) {
            write(path, defaults);
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            T loaded = GSON.fromJson(reader, type);
            if (loaded == null) {
                write(path, defaults);
                return defaults;
            }
            return loaded;
        } catch (Exception ex) {
            Constants.LOG.warn("Invalid config {}, regenerating defaults", fileName);
            write(path, defaults);
            return defaults;
        }
    }

    private static void write(Path path, Object value) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(value, writer);
        }
    }

    private static GlobalSettings defaultsGlobal() {
        GlobalSettings settings = new GlobalSettings();
        settings.ascendantLootTable = "minecraft:chests/end_city_treasure";
        settings.ascendantEffectsMinLevel = 50;
        settings.ascendantEffectsChance = 0.5;
        settings.randomEffects = true;
        settings.useLegacyHud = true;
        settings.hideHudFor = new ArrayList<>(List.of(""));
        settings.overlayXOffset = 0;
        settings.overlayYOffset = 0.45;
        settings.overlayZOffset = 0;
        return settings;
    }

    private static ScaleSettings defaultsScale() {
        ScaleSettings settings = new ScaleSettings();
        settings.scaleType = "both";
        settings.baseLevel = 1;
        settings.scaleFactor = 0.2;
        settings.scaleDistance = 2500;
        settings.dayFactor = 1;
        settings.xpModifier = 5;
        return settings;
    }

    private static DimensionsSettings defaultsDimensions() {
        DimensionsSettings settings = new DimensionsSettings();
        settings.dimensions = new ArrayList<>(List.of(
                new DimensionRange("minecraft:overworld", 1, 40),
                new DimensionRange("minecraft:the_nether", 20, 80),
                new DimensionRange("minecraft:the_end", 40, 100),
                new DimensionRange("aether:the_aether", 10, 60),
                new DimensionRange("twilightforest:twilight_forest", 10, 60),
                new DimensionRange("eternal_starlight:starligh", 10, 60),
                new DimensionRange("deeperdarker:otherside", 10, 60)
        ));
        return settings;
    }

    private static AttributesSettings defaultsAttributes() {
        AttributesSettings settings = new AttributesSettings();
        settings.attributes = new ArrayList<>(List.of(
                new AttributeRule("minecraft:generic.max_health", 2.0, 200, ""),
                new AttributeRule("minecraft:generic.movement_speed", 0.0015, 0.15, ""),
                new AttributeRule("minecraft:generic.armor", 0.1, 10, ""),
                new AttributeRule("minecraft:generic.attack_damage", 1.0, 100, "")
        ));
        return settings;
    }

    private static MobsListSettings defaultsMobsList() {
        MobsListSettings settings = new MobsListSettings();
        settings.banned = new ArrayList<>(List.of(
                "minecraft:dolphin",
                "minecraft:bat",
                "minecraft:pufferfish",
                "minecraft:salmon",
                "minecraft:cod"
        ));
        settings.deathMessages = false;
        settings.lockedMobs = new HashMap<>(Map.of(
                "minecraft:ender_dragon", 10.0,
                "minecraft:wither", 10.0,
                "minecraft:warden", 10.0
        ));
        settings.canBeAscendant = new ArrayList<>(List.of("minecraft:zombie", "minecraft:skeleton"));
        return settings;
    }

    public static GlobalSettings global() {
        return globalSettings;
    }

    public static ScaleSettings scale() {
        return scaleSettings;
    }

    public static DimensionsSettings dimensions() {
        return dimensionsSettings;
    }

    public static AttributesSettings attributes() {
        return attributesSettings;
    }

    public static MobsListSettings mobs() {
        return mobsListSettings;
    }

    public static final class GlobalSettings {
        public String ascendantLootTable;
        public double ascendantEffectsMinLevel;
        public double ascendantEffectsChance;
        public boolean randomEffects;
        public boolean useLegacyHud;
        public List<String> hideHudFor;
        public double overlayXOffset;
        public double overlayYOffset;
        public double overlayZOffset;
    }

    public static final class ScaleSettings {
        public String scaleType;
        public double baseLevel;
        public double scaleFactor;
        public double scaleDistance;
        public double dayFactor;
        public double xpModifier;
    }

    public static final class DimensionsSettings {
        public List<DimensionRange> dimensions;
    }

    public static final class DimensionRange {
        public String dimensionId;
        public double min;
        public double max;

        public DimensionRange(String dimensionId, double min, double max) {
            this.dimensionId = dimensionId;
            this.min = min;
            this.max = max;
        }
    }

    public static final class AttributesSettings {
        public List<AttributeRule> attributes;
    }

    public static final class AttributeRule {
        public String attributeId;
        public double valuePerLevel;
        public double maxValue;
        public String mobFilter;

        public AttributeRule(String attributeId, double valuePerLevel, double maxValue, String mobFilter) {
            this.attributeId = attributeId;
            this.valuePerLevel = valuePerLevel;
            this.maxValue = maxValue;
            this.mobFilter = mobFilter;
        }

        public ResourceLocation attributeKey() {
            return ResourceLocation.tryParse(attributeId);
        }
    }

    public static final class MobsListSettings {
        public List<String> banned;
        public boolean deathMessages;
        public Map<String, Double> lockedMobs;
        public List<String> canBeAscendant;
    }
}
