package com.nightbeam.remnants.mobs.config.edit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Reads and writes values at option paths: dots nest objects, {@code name[n]} indexes an array. */
public final class ConfigPaths {
    private ConfigPaths() {
    }

    /** True when {@code key} can be one segment of a path (map keys with dots or brackets cannot be edited). */
    public static boolean plain(String key) {
        return !key.isEmpty() && key.indexOf('.') < 0 && key.indexOf('[') < 0 && key.indexOf(']') < 0;
    }

    public static @Nullable JsonElement get(@Nullable JsonObject root, String path) {
        JsonElement current = root;
        for (String part : parts(path)) {
            current = step(current, part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /** Sets the value, creating missing objects on the way. False when the path runs through something else. */
    public static boolean set(JsonObject root, String path, JsonElement value) {
        List<String> parts = parts(path);
        JsonElement current = root;
        for (int i = 0; i < parts.size() - 1; i++) {
            String part = parts.get(i);
            JsonElement next = step(current, part);
            if (next == null && current instanceof JsonObject object && index(part) < 0) {
                next = new JsonObject();
                object.add(part, next);
            }
            if (next == null || !(next.isJsonObject() || next.isJsonArray())) {
                return false;
            }
            current = next;
        }
        String last = parts.get(parts.size() - 1);
        int index = index(last);
        if (index >= 0) {
            JsonElement array = current instanceof JsonObject object ? object.get(name(last)) : null;
            if (array == null || !array.isJsonArray() || index >= array.getAsJsonArray().size()) {
                return false;
            }
            array.getAsJsonArray().set(index, value);
            return true;
        }
        if (!(current instanceof JsonObject object)) {
            return false;
        }
        object.add(last, value);
        return true;
    }

    private static @Nullable JsonElement step(@Nullable JsonElement current, String part) {
        if (!(current instanceof JsonObject object)) {
            return null;
        }
        int index = index(part);
        if (index < 0) {
            return object.get(part);
        }
        JsonElement array = object.get(name(part));
        if (!(array instanceof JsonArray list) || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    static List<String> parts(String path) {
        List<String> parts = new ArrayList<>();
        for (String part : path.split("\\.")) {
            parts.add(part);
        }
        return parts;
    }

    private static int index(String part) {
        int open = part.indexOf('[');
        if (open < 0 || !part.endsWith("]")) {
            return -1;
        }
        try {
            return Integer.parseInt(part.substring(open + 1, part.length() - 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String name(String part) {
        int open = part.indexOf('[');
        return open < 0 ? part : part.substring(0, open);
    }
}
