# RPG Mob Leveling System 2.0.0

## Highlights

- Unified `LevelCalculationService`, `AttributeScalingService`, `EntityClassification`, `MobLevelService`
- RPG Attribute System 4.1 integration (`RasApi` read API, player scaling modes, combat balance)
- Gson JSON config (replaces Jauml on 1.20.1)
- Additive + percentage attribute scaling via idempotent modifiers
- Fixed distance (Euclidean) and day (`gameTime`) scaling
- Configurable rounding, namespace bans, hostile-only, structure/boss locks
- `/rml` debug commands + `RmlApi` public API
- Ascendant elites ported to common
- Fabric XP mixin parity
- Builds: 1.21.1 Fabric/NeoForge, 1.20.1 Fabric/Forge

## Migration

Existing Jauml configs on 1.20.1 are superseded by `config/rpgmoblevelingsystem/*.json` on first launch. Legacy bad `scaleDistance` values auto-migrate.

## Breaking changes

- Config format is JSON-only in 2.0
- Attribute application uses modifiers instead of permanent base mutation
