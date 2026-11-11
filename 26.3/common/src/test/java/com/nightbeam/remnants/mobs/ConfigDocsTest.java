package com.nightbeam.remnants.mobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.remnants.mobs.config.CommentedJson;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Keeps docs/CONFIG.md honest: every key of every file the mod writes must be documented there, every documented key
 * must still exist, and a single default listed in the docs must match the file. The files are written exactly as the
 * mod writes them, on a fresh install and next to an older config, into build/config-defaults/ for review.
 */
class ConfigDocsTest {
    private static final Pattern FILE_HEADING = Pattern.compile("^#+ .*`([a-z_]+\\.json)`");
    private static final Pattern BACKTICKED = Pattern.compile("`([^`]+)`");
    private static final Pattern PLACEHOLDER = Pattern.compile("<[^>]+>");

    private static Map<String, Map<String, JsonElement>> fresh;
    private static Map<String, Map<String, JsonElement>> upgrade;
    private static List<DocRow> rows;

    /** One row of a reference table: the file section it is in, its key (may hold placeholders) and its default. */
    record DocRow(String file, String key, String defaultCell, int line) {
        boolean matches(String codeKey) {
            StringBuilder regex = new StringBuilder();
            for (String part : key.split("\\.")) {
                if (regex.length() > 0) {
                    regex.append("\\.");
                }
                regex.append(PLACEHOLDER.matcher(part).matches() ? "[^.]+" : Pattern.quote(part));
            }
            return codeKey.matches(regex.toString());
        }

        boolean exact() {
            return !PLACEHOLDER.matcher(key).find();
        }
    }

    @BeforeAll
    static void writeFilesAndReadDocs() throws IOException {
        Path out = Path.of("build", "config-defaults");
        fresh = written(out.resolve("fresh"), false);
        upgrade = written(out.resolve("upgrade"), true);
        rows = parse(findDocs());
    }

    @Test
    void everyKeyIsDocumented() {
        Map<String, List<String>> missing = new TreeMap<>();
        for (Map<String, Map<String, JsonElement>> install : List.of(fresh, upgrade)) {
            install.forEach((file, keys) -> keys.keySet().forEach(key -> {
                if (rows.stream().noneMatch(row -> row.file().equals(file) && row.matches(key))) {
                    missing.computeIfAbsent(file, f -> new ArrayList<>()).add(key);
                }
            }));
        }
        if (!missing.isEmpty()) {
            fail("Config keys missing from docs/CONFIG.md (add a row to the file's \"| Key |\" table): " + dedupe(missing));
        }
    }

    @Test
    void everyDocumentedKeyExists() {
        List<String> stale = new ArrayList<>();
        for (DocRow row : rows) {
            Map<String, JsonElement> keys = fresh.get(row.file());
            if (keys == null) {
                stale.add(row.file() + " (line " + row.line() + "): no such file");
            } else if (keys.keySet().stream().noneMatch(row::matches)) {
                stale.add(row.file() + " → " + row.key() + " (line " + row.line() + ")");
            }
        }
        assertTrue(stale.isEmpty(), "docs/CONFIG.md documents keys the mod does not write: " + stale);
    }

    @Test
    void everyFileHasASection() {
        TreeSet<String> documented = rows.stream().map(DocRow::file).collect(Collectors.toCollection(TreeSet::new));
        assertEquals(new TreeSet<>(fresh.keySet()), documented, "every config file needs a section with a \"| Key |\" table");
    }

    /** A Default cell holding exactly one `value` must equal what the mod writes, on both kinds of install. */
    @Test
    void singleDefaultsMatchTheFiles() {
        List<String> wrong = new ArrayList<>();
        for (DocRow row : rows) {
            if (!row.exact()) {
                continue;
            }
            Matcher m = BACKTICKED.matcher(row.defaultCell());
            List<String> values = new ArrayList<>();
            while (m.find()) {
                values.add(m.group(1));
            }
            if (values.size() != 1) {
                continue;
            }
            JsonElement documented;
            try {
                documented = CommentedJson.parse(values.get(0));
            } catch (RuntimeException e) {
                wrong.add(row.key() + ": default `" + values.get(0) + "` is not a JSON value");
                continue;
            }
            for (Map<String, Map<String, JsonElement>> install : List.of(fresh, upgrade)) {
                JsonElement actual = install.getOrDefault(row.file(), Map.of()).get(row.key());
                if (actual != null && !same(documented, actual)) {
                    wrong.add(row.file() + " → " + row.key() + ": docs say " + documented + ", the mod writes " + actual
                            + (install == upgrade ? " (upgrade)" : " (fresh install)"));
                }
            }
        }
        assertTrue(wrong.isEmpty(), "docs/CONFIG.md defaults out of date: " + wrong);
    }

    private static boolean same(JsonElement documented, JsonElement actual) {
        if (documented.isJsonPrimitive() && actual.isJsonPrimitive()
                && documented.getAsJsonPrimitive().isNumber() && actual.getAsJsonPrimitive().isNumber()) {
            return Math.abs(documented.getAsDouble() - actual.getAsDouble()) < 1e-9;
        }
        return documented.equals(actual);
    }

    private static String dedupe(Map<String, List<String>> missing) {
        StringBuilder text = new StringBuilder();
        missing.forEach((file, keys) -> text.append("\n  ").append(file).append(": ")
                .append(keys.stream().distinct().sorted().collect(Collectors.joining(", "))));
        return text.toString();
    }

    /** Loads the config like the mod does and returns every key of every file it wrote, flattened. */
    private static Map<String, Map<String, JsonElement>> written(Path root, boolean olderConfig) throws IOException {
        deleteTree(root);
        Path dir = Files.createDirectories(root.resolve(RemnantMobs.MOD_ID));
        if (olderConfig) {
            // A 3.0 install: levels.json exists, so the 3.1 files are created with their systems off.
            Files.writeString(dir.resolve("levels.json"), "{}", StandardCharsets.UTF_8);
        }
        RemnantsConfig.load(root);
        Map<String, Map<String, JsonElement>> files = new TreeMap<>();
        try (Stream<Path> list = Files.list(dir)) {
            for (Path file : list.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                JsonElement parsed = CommentedJson.parse(Files.readString(file, StandardCharsets.UTF_8));
                Map<String, JsonElement> keys = new LinkedHashMap<>();
                flatten("", parsed, keys);
                files.put(file.getFileName().toString(), keys);
            }
        }
        return files;
    }

    /** Objects nest with dots; arrays of objects become {@code name[].field}; anything else (and {}) is a leaf. */
    static void flatten(String prefix, JsonElement element, Map<String, JsonElement> out) {
        if (element.isJsonObject() && element.getAsJsonObject().size() > 0) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                flatten(prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey(), entry.getValue(), out);
            }
        } else if (element.isJsonArray() && element.getAsJsonArray().size() > 0 && allObjects(element.getAsJsonArray())) {
            for (JsonElement item : element.getAsJsonArray()) {
                flatten(prefix + "[]", item, out);
            }
        } else {
            out.put(prefix, element);
        }
    }

    private static boolean allObjects(com.google.gson.JsonArray array) {
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                return false;
            }
        }
        return true;
    }

    /** Rows of every table headed "| Key |", under the last heading that names a `*.json` file. */
    static List<DocRow> parse(Path docs) throws IOException {
        List<String> lines = Files.readAllLines(docs, StandardCharsets.UTF_8);
        List<DocRow> found = new ArrayList<>();
        String file = null;
        int defaultColumn = -1;
        boolean table = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            Matcher heading = FILE_HEADING.matcher(line);
            if (line.startsWith("#")) {
                file = heading.find() ? heading.group(1) : null;
                table = false;
                continue;
            }
            if (!line.startsWith("|")) {
                table = false;
                continue;
            }
            List<String> cells = cells(line);
            if (!cells.isEmpty() && cells.get(0).equals("Key")) {
                table = file != null;
                defaultColumn = cells.indexOf("Default");
                i++; // the | --- | line
                continue;
            }
            if (!table) {
                continue;
            }
            Matcher key = BACKTICKED.matcher(cells.get(0));
            String defaultCell = defaultColumn >= 0 && defaultColumn < cells.size() ? cells.get(defaultColumn) : "";
            while (key.find()) {
                found.add(new DocRow(file, key.group(1), defaultCell, i + 1));
            }
        }
        assertTrue(found.size() > 50, "found only " + found.size() + " rows in " + docs);
        return found;
    }

    private static List<String> cells(String line) {
        String inner = line.substring(1, line.endsWith("|") ? line.length() - 1 : line.length());
        List<String> cells = new ArrayList<>();
        for (String cell : inner.split("(?<!\\\\)\\|", -1)) {
            cells.add(cell.trim());
        }
        return cells;
    }

    /** docs/CONFIG.md at the repository root, found from the workspace the tests run in. */
    private static Path findDocs() {
        for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path docs = dir.resolve("docs").resolve("CONFIG.md");
            if (Files.isRegularFile(docs)) {
                return docs;
            }
        }
        throw new AssertionError("docs/CONFIG.md not found above " + Path.of("").toAbsolutePath());
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
