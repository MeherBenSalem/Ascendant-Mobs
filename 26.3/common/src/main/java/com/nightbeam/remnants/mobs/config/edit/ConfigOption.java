package com.nightbeam.remnants.mobs.config.edit;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import org.jspecify.annotations.Nullable;

/**
 * One option of the config screen: a key of one config file with the rules its loader applies when reading it (type,
 * range, allowed values), so the screen and the server never write a value the loader would change or ignore.
 */
public final class ConfigOption {
    public enum Kind { TOGGLE, INTEGER, NUMBER, CHOICE }

    /** SERVER: read by the server (only the server's copy counts in multiplayer). CLIENT: read by each player's game. */
    public enum Side { SERVER, CLIENT }

    public static final double ANY = Double.POSITIVE_INFINITY;

    private final String group;
    private final String file;
    private final String path;
    private final String docKey;
    private final @Nullable String entry;
    private final Kind kind;
    private final double min;
    private final double max;
    private final List<String> choices;
    private final Side side;
    private final @Nullable String atLeast;
    private final @Nullable JsonElement defaultValue;

    ConfigOption(String group, String file, String path, String docKey, @Nullable String entry, Kind kind, double min, double max,
                 List<String> choices, Side side, @Nullable String atLeast, @Nullable JsonElement defaultValue) {
        this.group = group;
        this.file = file;
        this.path = path;
        this.docKey = docKey;
        this.entry = entry;
        this.kind = kind;
        this.min = min;
        this.max = max;
        this.choices = List.copyOf(choices);
        this.side = side;
        this.atLeast = atLeast;
        this.defaultValue = defaultValue;
    }

    /** The screen tab it is shown on. */
    public String group() {
        return group;
    }

    /** The config file, e.g. {@code levels.json}. */
    public String file() {
        return file;
    }

    /** Where the value sits in the file: dots nest, {@code [n]} indexes an array ({@code attributes[0].per_level}). */
    public String path() {
        return path;
    }

    /** The key as docs/CONFIG.md writes it, with placeholders: {@code dimensions.<dimension>.min}. */
    public String docKey() {
        return docKey;
    }

    /** The map entry this option belongs to (a dimension, rank, trait, event or attribute id), or null. */
    public @Nullable String entry() {
        return entry;
    }

    public Kind kind() {
        return kind;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public List<String> choices() {
        return choices;
    }

    public Side side() {
        return side;
    }

    /** A sibling path (same file) this value may not go below, like a band's max and its min. */
    public @Nullable String atLeast() {
        return atLeast;
    }

    /** The default, or null when there is none (an entry the user added). */
    public @Nullable JsonElement defaultValue() {
        return defaultValue == null ? null : defaultValue.deepCopy();
    }

    /** The translation key of its name; {@code .tooltip} is added for the description. */
    public String translationKey() {
        return "config.remnant_mobs." + file.substring(0, file.length() - ".json".length()) + "." + docKey;
    }

    /** True when both ends of the range are set, so a slider fits. */
    public boolean bounded() {
        return kind != Kind.TOGGLE && kind != Kind.CHOICE && Double.isFinite(min) && Double.isFinite(max);
    }

    /**
     * The value the loader would end up with for {@code raw}, as it should be written; null when the loader would ignore
     * {@code raw} (wrong type, not a number, not one of the choices).
     *
     * @param sibling the current number at another path of the same file (for {@link #atLeast()})
     */
    public @Nullable JsonElement normalize(@Nullable JsonElement raw, ToDoubleFunction<String> sibling) {
        if (raw == null || !raw.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive p = raw.getAsJsonPrimitive();
        switch (kind) {
            case TOGGLE:
                return p.isBoolean() ? new JsonPrimitive(p.getAsBoolean()) : null;
            case CHOICE: {
                if (!p.isString()) {
                    return null;
                }
                String value = p.getAsString().trim().toLowerCase(Locale.ROOT);
                return choices.contains(value) ? new JsonPrimitive(value) : null;
            }
            default: {
                if (!p.isNumber()) {
                    return null;
                }
                double value = p.getAsDouble();
                if (!Double.isFinite(value)) {
                    return null;
                }
                if (kind == Kind.INTEGER) {
                    value = Math.round(value);
                }
                double low = min;
                if (atLeast != null) {
                    double other = sibling.applyAsDouble(atLeast);
                    if (!Double.isNaN(other)) {
                        low = Math.max(low, other);
                    }
                }
                value = Math.max(low, Math.min(max, value));
                return number(value);
            }
        }
    }

    /** Reads text typed into a field ("12", "0.5", "on", "sqrt"); null when it is not a value of this option. */
    public @Nullable JsonElement parse(String text, ToDoubleFunction<String> sibling) {
        String t = text.trim();
        switch (kind) {
            case TOGGLE:
                if (t.equalsIgnoreCase("true") || t.equalsIgnoreCase("on")) {
                    return new JsonPrimitive(true);
                }
                if (t.equalsIgnoreCase("false") || t.equalsIgnoreCase("off")) {
                    return new JsonPrimitive(false);
                }
                return null;
            case CHOICE:
                return normalize(new JsonPrimitive(t), sibling);
            default:
                try {
                    return normalize(new JsonPrimitive(Double.parseDouble(t)), sibling);
                } catch (NumberFormatException e) {
                    return null;
                }
        }
    }

    /** A whole number is written without ".0", like the files the mod writes. */
    static JsonPrimitive number(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return new JsonPrimitive((long) value);
        }
        return new JsonPrimitive(value);
    }

    /** "128", "0.35", "on", "sqrt". */
    public static String text(@Nullable JsonElement value) {
        if (value == null || !value.isJsonPrimitive()) {
            return "";
        }
        JsonPrimitive p = value.getAsJsonPrimitive();
        if (p.isNumber()) {
            double d = p.getAsDouble();
            if (!Double.isFinite(d)) {
                return Double.isNaN(d) ? "" : d > 0 ? "∞" : "-∞";
            }
            if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                return Long.toString((long) d);
            }
            return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        }
        return p.getAsString();
    }

    @Override
    public String toString() {
        return file + " → " + path;
    }
}
