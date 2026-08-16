# Dimensions Config Reset 2.0.5 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop `dimensions_settings.json` from rewriting on every launch (#20) and ship Ascendant-Mobs 2.0.5 to GitHub, Modrinth, and CurseForge.

**Architecture:** Keep Gson `ModConfig` as source of truth. Remove Overworld `min<=0` auto-rewrite, add Gson-safe no-arg constructors, disable Forge Jauml dimensions bootstrap that re-seeds every startup. Bump both MultiLoader trees to 2.0.5 and use existing `publish_local.ps1` / `upload_local.ps1` flow.

**Tech Stack:** Java 17/21, Gradle MultiLoader, Gson, Forge Jauml (legacy), PowerShell publish scripts, Modrinth/CurseForge upload.

---

### Task 1: Failing regression test for dimensions Gson round-trip + zero min

**Files:**
- Create: `1.20.1/common/src/test/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfigDimensionsTest.java`
- Create: `1.21.1/common/src/test/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfigDimensionsTest.java` (same content)

- [ ] **Step 1: Write failing test** (fails until no-arg ctor exists / rewrite removed)

```java
package tn.nightbeam.rpgmoblevelingsystem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModConfigDimensionsTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
    void ensureDefaultsPreservesOverworldMinZero() throws Exception {
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

        String oldDir = System.getProperty("user.dir");
        Path oldCwd = Path.of("").toAbsolutePath();
        try {
            System.setProperty("user.dir", temp.toAbsolutePath().toString());
            // ModConfig uses Path.of relative to process CWD; change CWD via chdir if available,
            // otherwise invoke ensureDefaults from a helper that accepts root — for this repo,
            // run ensureDefaults after copying cwd by launching test with workingDir (Gradle).
            // Prefer testing via package-visible hook added in Task 2 if CWD cannot change.
            ModConfig.ensureDefaults();
            String after = Files.readString(configDir.resolve("dimensions_settings.json"), StandardCharsets.UTF_8);
            ModConfig.DimensionsSettings loaded = GSON.fromJson(after, ModConfig.DimensionsSettings.class);
            assertEquals(0.0, loaded.dimensions.get(0).min, 0.0001);
            assertEquals(2, loaded.dimensions.size());
            assertEquals("custom:dim", loaded.dimensions.get(1).dimensionId);
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }
}
```

Note: If `Path.of("config", ...)` ignores `user.dir`, Task 2 adds `ModConfig.overrideConfigRootForTests(Path)` package/public test hook used only from tests.

- [ ] **Step 2: Run test (expect fail or fragile CWD)**

```powershell
cd 1.20.1; .\gradlew.bat :common:test --tests tn.nightbeam.rpgmoblevelingsystem.config.ModConfigDimensionsTest
```

---

### Task 2: Fix ModConfig persistence (both trees)

**Files:**
- Modify: `1.20.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java`
- Modify: `1.21.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java`

- [ ] **Step 1: Remove Overworld min rewrite** — delete this block from `normalizeAndPersist`:

```java
if (dimensionsSettings.dimensions != null) {
    for (DimensionRange range : dimensionsSettings.dimensions) {
        if ("minecraft:overworld".equals(range.dimensionId) && range.min <= 0) {
            range.min = 1;
            changedDimensions = true;
        }
    }
}
```

Also remove unused `changedDimensions` write if no other writer sets it (keep flag only if still needed; otherwise remove flag + write).

- [ ] **Step 2: Add no-arg constructors**

```java
public DimensionRange() {
}

public DimensionRange(String dimensionId, double min, double max) {
    this.dimensionId = dimensionId;
    this.min = min;
    this.max = max;
}
```

Add no-arg ctors for `AttributeRule` and `LootBand` if missing.

- [ ] **Step 3: Optional test hook** if CWD tests fail:

```java
static void overrideConfigRootForTests(Path root) { /* package or test-only */ }
```

Prefer avoiding production hooks: fix CWD test by writing files under process working directory used by Gradle (`1.20.1/common`) inside `@TempDir` is hard — use `overrideConfigRootForTests` only if needed, cleared in `@AfterEach`.

- [ ] **Step 4: Re-run tests until green**

---

### Task 3: Disable Forge Jauml dimensions bootstrap

**Files:**
- Modify or delete: `1.20.1/forge/src/main/java/tn/nightbeam/rpgmoblevelingsystem/procedures/CreateOrReplaceDimensionsConfigProcedure.java`

- [ ] **Step 1: Remove `@Mod.EventBusSubscriber` and make `init` a no-op**, or delete the class entirely if nothing references it.

Preferred: delete the class file if no other references; otherwise:

```java
@SubscribeEvent
public static void init(FMLCommonSetupEvent event) {
    // No-op: Gson ModConfig owns dimensions_settings.json (issue #20)
}
```

- [ ] **Step 2: Confirm no compile references** via Gradle forge compile.

---

### Task 4: Version bump + patch notes

**Files:**
- Modify: `1.20.1/gradle.properties` — `version=2.0.5`
- Modify: `1.21.1/gradle.properties` — `version=2.0.5`
- Create: `Ascendant-Mobs-2.0.5-PatchNotes.md`

```markdown
# Ascendant-Mobs 2.0.5

**Platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge)

## Fixes
- `dimensions_settings.json` no longer rewrites on every launch (Forge 1.20.1 / all loaders)
- Overworld minimum level of 0 and custom dimension entries are preserved
- Disabled leftover Forge Jauml dimensions bootstrap that re-seeded defaults every startup

## Notes
- Drop-in replacement for 2.0.4 on all four loader jars
- GitHub issue #20
```

- [ ] **Step 1: Apply bumps + notes**
- [ ] **Step 2: Commit**

```powershell
git add -A
git commit -m "fix: preserve dimensions_settings.json across launches (#20)"
```

---

### Task 5: Build, push, GitHub release, platform upload

- [ ] **Step 1: Pull/push sync**

```powershell
git pull --ff-only origin main
git push origin main
```

- [ ] **Step 2: Local build + GitHub release**

```powershell
.\publish_local.ps1 -Version 2.0.5
```

Expected: four jars in `dist/` matching `*-2.0.5.jar`, GitHub release `v2.0.5`.

- [ ] **Step 3: Upload Modrinth + CurseForge**

```powershell
.\upload_local.ps1 -Version 2.0.5
```

Fallback: `gh workflow run publish.yml --repo MeherBenSalem/Ascendant-Mobs -f version=2.0.5`

- [ ] **Step 4: Close issue #20** with comment linking release.

---

## Spec coverage

| Spec item | Task |
|-----------|------|
| Remove Overworld min rewrite | Task 2 |
| Gson no-arg constructors | Task 2 |
| Disable Forge Jauml dimensions bootstrap | Task 3 |
| Version 2.0.5 + patch notes | Task 4 |
| Publish GitHub/Modrinth/CurseForge | Task 5 |
| Regression test for min=0 preserve | Task 1 |
