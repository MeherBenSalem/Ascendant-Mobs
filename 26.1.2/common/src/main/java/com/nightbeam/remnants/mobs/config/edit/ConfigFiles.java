package com.nightbeam.remnants.mobs.config.edit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.remnants.mobs.config.ClientConfig;
import com.nightbeam.remnants.mobs.config.CommentedJson;
import com.nightbeam.remnants.mobs.config.DifficultyConfig;
import com.nightbeam.remnants.mobs.config.ElitesConfig;
import com.nightbeam.remnants.mobs.config.EventsConfig;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.LootConfig;
import com.nightbeam.remnants.mobs.config.ProgressionConfig;
import com.nightbeam.remnants.mobs.config.RanksConfig;
import com.nightbeam.remnants.mobs.config.RewardsConfig;
import com.nightbeam.remnants.mobs.config.TraitsConfig;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Side;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reads and writes the config files for the config screen and the server, through the same specs the loader uses, so
 * the JSON files stay the single source of truth: comments, unknown keys and untouched values are kept.
 */
public final class ConfigFiles {
    public static final List<String> FILES = List.of("levels.json", "ranks.json", "traits.json", "rewards.json", "client.json",
            "difficulty.json", "events.json", "progression.json", "elites.json", "loot.json");

    private ConfigFiles() {
    }

    /** The spec {@code RemnantsConfig.load} uses for {@code file}. */
    public static CommentedJson spec(String file, boolean fresh, LevelsConfig levels) {
        return switch (file) {
            case "levels.json" -> LevelsConfig.spec();
            case "ranks.json" -> RanksConfig.spec();
            case "traits.json" -> TraitsConfig.spec();
            case "rewards.json" -> RewardsConfig.spec();
            case "client.json" -> ClientConfig.spec();
            case "difficulty.json" -> DifficultyConfig.spec(fresh, levels);
            case "events.json" -> EventsConfig.spec(fresh);
            case "progression.json" -> ProgressionConfig.spec(fresh);
            case "elites.json" -> ElitesConfig.spec(fresh);
            case "loot.json" -> LootConfig.spec(fresh);
            default -> throw new IllegalArgumentException("Not a Remnants config file: " + file);
        };
    }

    /** Every file's values as the loader sees them (merged over the defaults). Nothing is written. */
    public static Map<String, JsonObject> read(Path dir) {
        Map<String, JsonObject> files = new TreeMap<>();
        JsonObject levels = LevelsConfig.spec().read(dir.resolve("levels.json"));
        LevelsConfig parsed = LevelsConfig.from(levels);
        for (String file : FILES) {
            files.put(file, file.equals("levels.json") ? levels : spec(file, false, parsed).read(dir.resolve(file)));
        }
        return files;
    }

    /** The defaults of a fresh install; the difficulty factors start from {@code levels} like the loader's do. */
    public static Map<String, JsonObject> defaults(JsonObject levels) {
        LevelsConfig parsed = LevelsConfig.from(levels);
        Map<String, JsonObject> files = new TreeMap<>();
        for (String file : FILES) {
            files.put(file, spec(file, true, parsed).defaults());
        }
        return files;
    }

    /** What {@link #apply} did: how many values it wrote, and the ones it refused. */
    public record Result(int written, List<String> refused) {
    }

    /**
     * Writes {@code changes} (file → path → value) into the files in {@code dir}. Each value is checked again against
     * the options built from the files as they are now: paths the screen does not edit, sides not in {@code sides} and
     * values the loader would not read are refused; accepted values are written normalized. Each file is read through
     * its spec (missing keys come back), changed, and replaced in one move. Reload the config afterwards.
     */
    public static Result apply(Path dir, Map<String, Map<String, JsonElement>> changes, Set<Side> sides) throws IOException {
        Map<String, JsonObject> current = read(dir);
        LevelsConfig levels = LevelsConfig.from(current.get("levels.json"));
        ConfigEdit edit = new ConfigEdit(current, defaults(current.get("levels.json")));
        List<String> refused = new ArrayList<>();
        Map<String, Map<String, JsonElement>> accepted = new LinkedHashMap<>();
        changes.forEach((file, values) -> values.forEach((path, value) -> {
            if (FILES.contains(file) && spec(file, false, levels).readStrict(dir.resolve(file)) == null) {
                // The loader ignores a file it cannot parse and leaves it alone; so does the screen.
                refused.add(file + " " + path + " (the file is not valid JSON)");
                return;
            }
            ConfigOption option = ConfigOptions.find(edit.options(), file, path);
            if (option == null || !sides.contains(option.side()) || !edit.set(option, value)) {
                refused.add(file + " " + path);
            }
        }));
        // Everything that differs now, including options that follow another (a band's max).
        for (ConfigOption option : edit.options()) {
            if (edit.changed(option) && sides.contains(option.side())) {
                accepted.computeIfAbsent(option.file(), f -> new LinkedHashMap<>()).put(option.path(), edit.value(option));
            }
        }
        int written = 0;
        for (Map.Entry<String, Map<String, JsonElement>> file : accepted.entrySet()) {
            CommentedJson spec = spec(file.getKey(), false, levels);
            Path path = dir.resolve(file.getKey());
            JsonObject json = spec.readStrict(path);
            if (json == null) {
                file.getValue().keySet().forEach(key -> refused.add(file.getKey() + " " + key + " (the file is not valid JSON)"));
                continue;
            }
            for (Map.Entry<String, JsonElement> value : file.getValue().entrySet()) {
                if (ConfigPaths.set(json, value.getKey(), value.getValue())) {
                    written++;
                } else {
                    refused.add(file.getKey() + " " + value.getKey());
                }
            }
            write(path, spec.write(json));
        }
        return new Result(written, refused);
    }

    private static void write(Path file, String text) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, text, StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Values of the server-side files only, for sending to a client. */
    public static Map<String, JsonObject> serverFiles(Map<String, JsonObject> files) {
        Map<String, JsonObject> out = new TreeMap<>(files);
        out.remove("client.json");
        return out;
    }
}
