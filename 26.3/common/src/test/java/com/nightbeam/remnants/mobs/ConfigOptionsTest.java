package com.nightbeam.remnants.mobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.nightbeam.remnants.mobs.config.ClientConfig;
import com.nightbeam.remnants.mobs.config.DifficultyConfig;
import com.nightbeam.remnants.mobs.config.ElitesConfig;
import com.nightbeam.remnants.mobs.config.EventsConfig;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.LootConfig;
import com.nightbeam.remnants.mobs.config.ProgressionConfig;
import com.nightbeam.remnants.mobs.config.RanksConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.RewardsConfig;
import com.nightbeam.remnants.mobs.config.TraitsConfig;
import com.nightbeam.remnants.mobs.config.edit.ConfigEdit;
import com.nightbeam.remnants.mobs.config.edit.ConfigFiles;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Kind;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Side;
import com.nightbeam.remnants.mobs.config.edit.ConfigOptions;
import com.nightbeam.remnants.mobs.config.edit.ConfigPaths;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The config screen's model: ranges against the loaders, normalizing, reset, and writing the files. */
class ConfigOptionsTest {
    /** Keys whose loader enforces no range: the screen uses the documented range instead (stricter, never looser). */
    private static final Set<String> SCREEN_ONLY_LIMITS = Set.of("structures.<structure>");

    private static Map<String, JsonObject> defaults() {
        return ConfigFiles.defaults(LevelsConfig.spec().defaults());
    }

    private static List<ConfigOption> options() {
        Map<String, JsonObject> defaults = defaults();
        return ConfigOptions.all(defaults, defaults);
    }

    /** Loads {@code json} with the real loader of {@code file}. */
    private static Object load(String file, JsonObject json) {
        return switch (file) {
            case "levels.json" -> LevelsConfig.from(json);
            case "ranks.json" -> RanksConfig.from(json);
            case "traits.json" -> TraitsConfig.from(json);
            case "rewards.json" -> RewardsConfig.from(json);
            case "client.json" -> ClientConfig.from(json);
            case "difficulty.json" -> DifficultyConfig.from(json);
            case "events.json" -> EventsConfig.from(json);
            case "progression.json" -> ProgressionConfig.from(json);
            case "elites.json" -> ElitesConfig.from(json);
            case "loot.json" -> LootConfig.from(json);
            default -> throw new IllegalArgumentException(file);
        };
    }

    /** Everything the loader made of the file: every field, deeply, as text. Equal text means the same effect. */
    private static String effect(String file, JsonObject json) {
        return fingerprint(load(file, json));
    }

    private static String fingerprint(Object value) {
        if (value instanceof TraitsConfig.Settings settings) {
            // The loader keeps the trait's raw JSON for its own numbers; enabled, weight, min_rank and exclude are
            // read into the record's fields, so only those fields count for them.
            JsonObject numbers = settings.values().deepCopy();
            List.of("enabled", "weight", "min_rank", "exclude").forEach(numbers::remove);
            return "Settings[" + settings.enabled() + "," + settings.weight() + "," + settings.minRank() + "," + settings.exclude() + "," + numbers + "]";
        }
        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof CharSequence
                || value instanceof Enum<?> || value instanceof JsonElement || value instanceof Record) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            return new TreeMap<>(map.entrySet().stream().collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                    e -> fingerprint(e.getValue())))).toString();
        }
        if (value instanceof Collection<?> list) {
            return list.stream().map(ConfigOptionsTest::fingerprint).toList().toString();
        }
        StringBuilder out = new StringBuilder(value.getClass().getSimpleName()).append('{');
        for (Field field : value.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                out.append(field.getName()).append('=').append(fingerprint(field.get(value))).append(';');
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
        return out.append('}').toString();
    }

    private static String with(ConfigOption option, JsonElement value) {
        JsonObject json = defaults().get(option.file()).deepCopy();
        assertTrue(ConfigPaths.set(json, option.path(), value), option.toString());
        return effect(option.file(), json);
    }

    private static JsonPrimitive num(double value) {
        return new JsonPrimitive(value);
    }

    private static double step(ConfigOption option) {
        return option.kind() == Kind.INTEGER ? 1 : 0.001;
    }

    private static double sibling(ConfigOption option) {
        return option.atLeast() == null ? Double.NaN
                : ConfigPaths.get(defaults().get(option.file()), option.atLeast()).getAsDouble();
    }

    /**
     * The heart of "never write a value the loader would reject": for every option, the loader must keep every value
     * inside the screen's range as it is (the values just inside each end load differently), and where the loader
     * clamps, it must clamp at the same place (the value just outside loads like the end itself).
     */
    @Test
    void rangesMatchTheLoaders() {
        List<String> problems = new ArrayList<>();
        for (ConfigOption option : options()) {
            switch (option.kind()) {
                case TOGGLE -> {
                    if (with(option, new JsonPrimitive(true)).equals(with(option, new JsonPrimitive(false)))) {
                        problems.add(option + ": the loader ignores it");
                    }
                }
                case CHOICE -> {
                    Set<String> effects = option.choices().stream().map(c -> with(option, new JsonPrimitive(c))).collect(Collectors.toSet());
                    if (effects.size() != option.choices().size()) {
                        problems.add(option + ": the loader does not tell all of " + option.choices() + " apart");
                    }
                }
                default -> {
                    double step = step(option);
                    boolean screenOnly = SCREEN_ONLY_LIMITS.contains(option.docKey())
                            || (option.file().equals("traits.json") && !option.docKey().startsWith("<trait>."));
                    double low = option.atLeast() != null ? Math.max(option.min(), sibling(option)) : option.min();
                    if (Double.isFinite(low)) {
                        if (with(option, num(low)).equals(with(option, num(low + step)))) {
                            problems.add(option + ": the loader changes " + (low + step) + ", inside the screen's range");
                        }
                        boolean clampsBelow = with(option, num(low - step)).equals(with(option, num(low)));
                        if (clampsBelow == screenOnly) {
                            problems.add(option + (screenOnly ? ": the loader now limits it at " : ": the loader does not stop at ") + low);
                        }
                    }
                    if (Double.isFinite(option.max())) {
                        double high = option.max();
                        if (with(option, num(high)).equals(with(option, num(high - step)))) {
                            problems.add(option + ": the loader changes " + (high - step) + ", inside the screen's range");
                        }
                        boolean clampsAbove = with(option, num(high + step)).equals(with(option, num(high)));
                        if (clampsAbove == screenOnly) {
                            problems.add(option + (screenOnly ? ": the loader now limits it at " : ": the loader does not stop at ") + high);
                        }
                    }
                    if (!Double.isFinite(low) && !Double.isFinite(option.max())
                            && with(option, num(3)).equals(with(option, num(7)))) {
                        problems.add(option + ": the loader ignores it");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), "Screen ranges disagree with the loaders:\n  " + String.join("\n  ", problems));
    }

    @Test
    void everyOptionIsDocumentedAndTranslated() throws IOException {
        List<ConfigDocsTest.DocRow> rows = ConfigDocsTest.parse(ConfigDocsTest.findDocs());
        JsonObject lang = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/remnant_mobs/lang/en_us.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        List<String> missing = new ArrayList<>();
        for (ConfigOption option : options()) {
            if (rows.stream().noneMatch(row -> row.file().equals(option.file()) && row.key().equals(option.docKey()))) {
                missing.add("docs: " + option.file() + " " + option.docKey());
            }
            for (String key : List.of(option.translationKey(), option.translationKey() + ".tooltip")) {
                if (!lang.has(key)) {
                    missing.add("en_us.json: " + key);
                }
            }
        }
        for (String group : ConfigOptions.GROUPS) {
            if (!lang.has("config.remnant_mobs.group." + group)) {
                missing.add("en_us.json: config.remnant_mobs.group." + group);
            }
        }
        assertTrue(missing.isEmpty(), "Missing: " + missing.stream().distinct().toList());
        assertTrue(options().stream().map(ConfigOption::group).allMatch(ConfigOptions.GROUPS::contains));
    }

    @Test
    void normalizesLikeTheLoader() {
        List<ConfigOption> options = options();
        ConfigOption spread = ConfigOptions.find(options, "levels.json", "spread");
        ConfigOption weight = ConfigOptions.find(options, "levels.json", "players.weight");
        ConfigOption mode = ConfigOptions.find(options, "levels.json", "players.mode");
        ConfigOption enabled = ConfigOptions.find(options, "difficulty.json", "enabled");
        assertNotNull(spread);
        assertNotNull(weight);
        assertNotNull(mode);
        assertNotNull(enabled);
        assertEquals(new JsonPrimitive(3), spread.normalize(num(2.6), p -> Double.NaN));
        assertEquals(new JsonPrimitive(0), spread.normalize(num(-4), p -> Double.NaN));
        assertEquals(new JsonPrimitive(1), weight.normalize(num(3.5), p -> Double.NaN));
        assertEquals(new JsonPrimitive(0.35), weight.normalize(num(0.35), p -> Double.NaN));
        assertNull(weight.normalize(new JsonPrimitive("lots"), p -> Double.NaN));
        assertNull(weight.normalize(num(Double.NaN), p -> Double.NaN));
        assertEquals(new JsonPrimitive("nearest"), mode.normalize(new JsonPrimitive(" Nearest "), p -> Double.NaN));
        assertNull(mode.normalize(new JsonPrimitive("loudest"), p -> Double.NaN));
        assertNull(enabled.normalize(num(1), p -> Double.NaN));
        assertEquals(new JsonPrimitive(false), enabled.parse("off", p -> Double.NaN));
        assertEquals(new JsonPrimitive(128), ConfigFiles.defaults(LevelsConfig.spec().defaults()).get("levels.json")
                .getAsJsonObject("distance").get("blocks_per_level"));
        assertEquals("0.35", ConfigOption.text(num(0.35)));
        assertEquals("128", ConfigOption.text(num(128.0)));
        assertNull(spread.parse("two", p -> Double.NaN));
        // Every option's range ends can be shown, open ones included (an infinite end once crashed the screen).
        for (ConfigOption option : options) {
            ConfigOption.text(num(option.min()));
            ConfigOption.text(num(option.max()));
            ConfigOption.text(option.defaultValue());
        }
        assertEquals("∞", ConfigOption.text(num(Double.POSITIVE_INFINITY)));
    }

    @Test
    void editResetsAndKeepsBandsValid() {
        Map<String, JsonObject> defaults = defaults();
        ConfigEdit edit = new ConfigEdit(defaults, defaults);
        ConfigOption min = ConfigOptions.find(edit.options(), "levels.json", "dimensions.minecraft:overworld.min");
        ConfigOption max = ConfigOptions.find(edit.options(), "levels.json", "dimensions.minecraft:overworld.max");
        ConfigOption hud = ConfigOptions.find(edit.options(), "progression.json", "meter.hud");
        assertNotNull(min);
        assertNotNull(max);
        assertNotNull(hud);
        assertTrue(edit.isDefault(min));
        assertFalse(edit.dirty());

        assertTrue(edit.set(min, num(75)));
        assertEquals(75, edit.value(max).getAsInt(), "the band's max follows its min");
        assertTrue(edit.set(max, num(10)));
        assertEquals(75, edit.value(max).getAsInt(), "a max below the min is raised to it, as the loader does");
        assertTrue(edit.set(hud, new JsonPrimitive(false)));
        assertFalse(edit.set(hud, new JsonPrimitive("maybe")));

        Map<String, Map<String, JsonElement>> server = edit.changes(Side.SERVER);
        Map<String, Map<String, JsonElement>> client = edit.changes(Side.CLIENT);
        assertEquals(Set.of("levels.json"), server.keySet());
        assertEquals(Set.of("dimensions.minecraft:overworld.min", "dimensions.minecraft:overworld.max"), server.get("levels.json").keySet());
        assertEquals(Map.of("meter.hud", new JsonPrimitive(false)), client.get("progression.json"));

        assertTrue(edit.reset(min));
        assertTrue(edit.reset(max));
        assertTrue(edit.reset(hud));
        assertTrue(edit.isDefault(min));
        assertFalse(edit.dirty(), "values back at their saved value are no change");
    }

    @Test
    void entriesTheUserAddedAreEditable() {
        Map<String, JsonObject> defaults = defaults();
        Map<String, JsonObject> values = new TreeMap<>(defaults);
        JsonObject levels = defaults.get("levels.json").deepCopy();
        levels.getAsJsonObject("dimensions").add("aether:the_aether", JsonParser.parseString("{\"min\": 10}"));
        levels.getAsJsonObject("structures").addProperty("minecraft:igloo", 9);
        values.put("levels.json", levels);
        ConfigEdit edit = new ConfigEdit(values, defaults);
        ConfigOption min = ConfigOptions.find(edit.options(), "levels.json", "dimensions.aether:the_aether.min");
        ConfigOption max = ConfigOptions.find(edit.options(), "levels.json", "dimensions.aether:the_aether.max");
        ConfigOption igloo = ConfigOptions.find(edit.options(), "levels.json", "structures.minecraft:igloo");
        assertNotNull(min);
        assertNotNull(max);
        assertNotNull(igloo);
        assertEquals(10, edit.value(min).getAsInt());
        assertEquals(60, edit.value(max).getAsInt(), "a missing key shows what the loader uses: default_band");
        assertEquals(1, min.defaultValue().getAsInt());
        assertNull(igloo.defaultValue(), "no default for an entry the mod does not ship");
        assertFalse(edit.reset(igloo));
    }

    @Test
    void applyWritesOnlyWhatTheLoaderAccepts(@TempDir Path root) throws IOException {
        RemnantsConfig.load(root);
        Path dir = root.resolve(RemnantMobs.MOD_ID);
        Files.writeString(dir.resolve("ranks.json"), "{ \"enabled\": false, ", StandardCharsets.UTF_8);
        String brokenRanks = Files.readString(dir.resolve("ranks.json"), StandardCharsets.UTF_8);

        Map<String, Map<String, JsonElement>> changes = new TreeMap<>();
        changes.put("levels.json", new TreeMap<>(Map.of("spread", num(5), "players.weight", num(3), "mobs.exclude", num(1),
                "nonsense.key", num(1))));
        changes.put("client.json", new TreeMap<>(Map.of("nameplates.scale", num(1.5))));
        changes.put("ranks.json", new TreeMap<>(Map.of("enabled", new JsonPrimitive(true))));
        ConfigFiles.Result result = ConfigFiles.apply(dir, changes, Set.of(Side.SERVER));

        assertEquals(2, result.written());
        assertTrue(result.refused().contains("levels.json mobs.exclude"), result.refused().toString());
        assertTrue(result.refused().contains("levels.json nonsense.key"), result.refused().toString());
        assertTrue(result.refused().contains("client.json nameplates.scale"), "client options are not the server's to write");
        assertTrue(result.refused().stream().anyMatch(r -> r.startsWith("ranks.json enabled")), "a broken file is not overwritten");
        assertEquals(brokenRanks, Files.readString(dir.resolve("ranks.json"), StandardCharsets.UTF_8));

        String text = Files.readString(dir.resolve("levels.json"), StandardCharsets.UTF_8);
        assertTrue(text.contains("// Each mob's level varies"), "comments are kept");
        RemnantsConfig.load(root);
        assertEquals(5, RemnantsConfig.levels().spread);
        assertEquals(1.0, RemnantsConfig.levels().playerWeight);
        assertEquals(1.0, RemnantsConfig.client().scale);
        assertNotEquals(0, Files.size(dir.resolve("client.json")));
    }
}
