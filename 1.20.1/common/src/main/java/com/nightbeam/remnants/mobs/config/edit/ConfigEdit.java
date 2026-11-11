package com.nightbeam.remnants.mobs.config.edit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Side;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;

/**
 * What the config screen edits: the values it started from, the options, and the changes made so far. Every value
 * goes through {@link ConfigOption#normalize} first, so a change is always one the loader would read as written.
 */
public final class ConfigEdit {
    private final Map<String, JsonObject> original;
    private final List<ConfigOption> options;
    private final Map<ConfigOption, JsonElement> changes = new LinkedHashMap<>();

    public ConfigEdit(Map<String, JsonObject> values, Map<String, JsonObject> defaults) {
        this.original = new TreeMap<>();
        values.forEach((file, json) -> original.put(file, json.deepCopy()));
        this.options = ConfigOptions.all(original, defaults);
    }

    public List<ConfigOption> options() {
        return options;
    }

    /** The value the game uses now (the file's value as the loader reads it, or the default if it cannot be read). */
    public @Nullable JsonElement saved(ConfigOption option) {
        JsonElement raw = ConfigPaths.get(original.get(option.file()), option.path());
        JsonElement value = option.normalize(raw, path -> number(original, option.file(), path));
        return value != null ? value : option.defaultValue();
    }

    /** The value shown: the change if there is one, else the saved value. */
    public @Nullable JsonElement value(ConfigOption option) {
        JsonElement changed = changes.get(option);
        return changed != null ? changed.deepCopy() : saved(option);
    }

    /**
     * Sets a value. It is normalized like the loader would read it; options that may not go below it (a band's max)
     * follow. Returns false (and changes nothing) when the loader would not accept {@code raw} at all.
     */
    public boolean set(ConfigOption option, @Nullable JsonElement raw) {
        JsonElement value = option.normalize(raw, path -> current(option.file(), path));
        if (value == null) {
            return false;
        }
        if (value.equals(saved(option))) {
            changes.remove(option);
        } else {
            changes.put(option, value);
        }
        for (ConfigOption other : options) {
            if (other.file().equals(option.file()) && option.path().equals(other.atLeast())) {
                JsonElement current = value(other);
                if (current != null) {
                    set(other, current);
                }
            }
        }
        return true;
    }

    /** Back to the default. False when the option has none (an entry the user added). */
    public boolean reset(ConfigOption option) {
        JsonElement value = option.defaultValue();
        return value != null && set(option, value);
    }

    public boolean isDefault(ConfigOption option) {
        JsonElement def = option.defaultValue();
        JsonElement current = value(option);
        if (def == null || current == null) {
            return def == current;
        }
        if (def.isJsonPrimitive() && current.isJsonPrimitive() && def.getAsJsonPrimitive().isNumber()
                && current.getAsJsonPrimitive().isNumber()) {
            return Math.abs(def.getAsDouble() - current.getAsDouble()) < 1e-9;
        }
        return def.equals(current);
    }

    public boolean changed(ConfigOption option) {
        return changes.containsKey(option);
    }

    public boolean dirty() {
        return !changes.isEmpty();
    }

    public void discard() {
        changes.clear();
    }

    /** The changes of one side, as file → path → value, ready to be written or sent. */
    public Map<String, Map<String, JsonElement>> changes(Side side) {
        Map<String, Map<String, JsonElement>> out = new TreeMap<>();
        changes.forEach((option, value) -> {
            if (option.side() == side) {
                out.computeIfAbsent(option.file(), f -> new LinkedHashMap<>()).put(option.path(), value.deepCopy());
            }
        });
        return out;
    }

    private double current(String file, String path) {
        for (Map.Entry<ConfigOption, JsonElement> change : changes.entrySet()) {
            if (change.getKey().file().equals(file) && change.getKey().path().equals(path)) {
                return change.getValue().getAsDouble();
            }
        }
        return number(original, file, path);
    }

    private static double number(Map<String, JsonObject> files, String file, String path) {
        JsonElement value = ConfigPaths.get(files.get(file), path);
        if (value instanceof JsonPrimitive p && p.isNumber()) {
            return p.getAsDouble();
        }
        return Double.NaN;
    }
}
