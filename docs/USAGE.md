# RPG Mob Leveling System 2.0.0

## Overview

RPG Mob Leveling System assigns levels to hostile mobs based on distance from spawn, world age, dimension, nearby player RPG progression (RPG Attribute System), and configuration. Levels scale mob attributes using additive or percentage modifiers without corrupting base entity stats.

## Config (`config/rpgmoblevelingsystem/`)

| File | Purpose |
|------|---------|
| `scale_settings.json` | Scale type, distance, day factor, rounding, player scaling, balance |
| `dimensions_settings.json` | Per-dimension min/max levels |
| `attributes_settings.json` | Attribute rules (`additive` or `percent`, `hostileOnly`) |
| `mobs_list_settings.json` | Banned entities/namespaces, boss locks, structure mins |
| `global_settings.json` | Overlay, Jade, name format |
| `loot_by_level.json` | Optional level-band custom drops |

### Player scaling (RAS)

Set `playerScaleMode` to `nearest`, `highest_nearby`, or `average_nearby` when RPG Attribute System is installed. Requires `rpg_attribute_system` 4.1+.

### Rounding & distance

Distance scaling uses **Euclidean blocks / scaleDistance** (not squared distance). `roundingMode`: `floor`, `ceil`, or `round`.

## Commands (op level 2)

- `/rml inspect <entity>` — level calculation breakdown
- `/rml recalculate <entity>` — force re-apply level
- `/rml config` — validate config
- `/rml distance` — distance from world spawn

## Public API

Package `tn.nightbeam.rpgmoblevelingsystem.api.RmlApi`:

- `getLevel(Entity)`
- `recalculate(Entity)`
- `getDebugBreakdown(Entity)`
- `getDisplayLevel(Level, Entity)`

## Calendar item

Right-click the Calendar item to print current day, dimension, and configured min/max levels.

## Jade

When Jade is installed, mob levels are exposed via `am_level_*` entity tags. Use `global_settings.json` → `jadeEnabled`.

## Loaders & versions

| MC | Loaders |
|----|---------|
| 1.21.1 | Fabric, NeoForge |
| 1.20.1 | Fabric, Forge |
