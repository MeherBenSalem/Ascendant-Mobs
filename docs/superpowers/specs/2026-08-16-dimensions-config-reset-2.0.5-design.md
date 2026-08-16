# Design: Fix dimensions_settings.json reset + release 2.0.5

**Date:** 2026-08-16  
**Issue:** [#20](https://github.com/MeherBenSalem/Ascendant-Mobs/issues/20)  
**Version:** 2.0.5  
**Approach:** A — Fix Gson persistence + stop Forge Jauml dimensions bootstrap  

## Problem

On Forge 1.20.1, `dimensions_settings.json` rewrites on every launch. Players lose edits (Overworld start level, added dimensions). Mid-load the file appears truncated, then completes as bootstrap finishes writing.

## Root causes

1. **`ModConfig.normalizeAndPersist`** forces Overworld `min <= 0` → `1` and rewrites the JSON every load.
2. **Gson DTO risk:** `DimensionRange` (and similar nested types) lack no-arg constructors, which can break or weaken deserialization and trigger default regeneration.
3. **Legacy Forge Jauml bootstrap:** `CreateOrReplaceDimensionsConfigProcedure` checks array key `"settings"` but writes `"dimensions"`, so the existence check never succeeds and defaults are re-seeded every startup (matches progressive mid-load file growth).

Source of truth for 2.x is Gson under `config/rpgmoblevelingsystem/`. Jauml under `RpgMobLevelingSystem` is leftover for some Forge procedures and must not keep rewriting dimensions.

## Goals

- Player edits to `dimensions_settings.json` persist across restarts.
- Overworld `min` of `0` (or any custom value) is allowed and not auto-corrected.
- No dual-writer fight on dimensions for Forge 1.20.1.
- Ship all four loader jars as **2.0.5** to Modrinth + CurseForge.
- Close #20 with release notes.

## Non-goals

- Full removal/migration of all Jauml Forge procedures (Approach C).
- Changing dimension scaling gameplay formulas.
- Republishing unpublished 2.0.4 content as its own release number.

## Design

### Config persistence (`ModConfig` in 1.20.1 and 1.21.1)

1. Remove the Overworld `min <= 0 → 1` rewrite block from `normalizeAndPersist`.
2. Add Gson-safe no-arg constructors for nested config types used in JSON, especially `DimensionRange` (and any sibling DTOs that only have parameterized constructors if they deserialize from the same files).
3. Keep existing null-map/list normalization for mobs list and other files; do not broaden scope.

### Forge Jauml dimensions bootstrap

1. Disable `CreateOrReplaceDimensionsConfigProcedure` so it no longer runs on `FMLCommonSetupEvent` (delete class or empty/no-op the `@SubscribeEvent` path; prefer removing the subscriber so it cannot re-seed).
2. Leave other `CreateOrReplace*` procedures untouched in this release unless they are proven to rewrite the same Gson path.

### Version & release

1. Ensure working tree is on latest `main` (pull/merge first).
2. Bump `version=2.0.5` in `1.20.1/gradle.properties` and `1.21.1/gradle.properties`.
3. Add `Ascendant-Mobs-2.0.5-PatchNotes.md` describing the #20 fix.
4. Commit, push to `main`.
5. Build + GitHub Release via `.\publish_local.ps1 -Version 2.0.5`.
6. Publish to Modrinth + CurseForge via `.\upload_local.ps1 -Version 2.0.5` (and/or `gh workflow run publish.yml -f version=2.0.5`).
7. Comment on / close GitHub issue #20.

## Testing / acceptance

- Unit or focused test: loading a dimensions config with Overworld `min = 0` does not rewrite `min` to `1`.
- Unit or focused test: custom dimensions list round-trips through read → normalize → no unnecessary write (or write preserves content).
- Manual/build verification: `publish_local.ps1` produces four `rpgmoblevelingsystem-*-2.0.5.jar` artifacts.
- Platforms: Modrinth and CurseForge show 2.0.5 for all supported loaders.

## Risks

- Some leftover Forge Jauml readers may still ignore Gson dimensions until a later full migration; gameplay for levels may still read Jauml in older procedure paths. This release prioritizes stopping the rewrite of the player-facing Gson file reported in #20.
- If CurseForge/Modrinth tokens are missing locally, fall back to `publish.yml` with repo secrets.

## Success criteria

- #20 fixed in code for both MultiLoader trees.
- 2.0.5 released on GitHub, Modrinth, and CurseForge.
- Patch notes mention dimensions config persistence fix.
