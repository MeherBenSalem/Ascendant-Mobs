=============================================
## Changelog 1.2.1
=============================================

## Fixes
- **Fixed** an issue with `xp_modifier` that caused it not to function properly.

## Additions
- **Added** a custom attribute for all mobs: `ascendant_mobs:amlevel_attribute`
  - This attribute stores the mob's level.
  - Allows spawning mobs at specific levels using vanilla commands.

### Example Commands
#### Minecraft 1.16.5 - 1.20:
```mcfunction
/summon minecraft:sheep ~ ~ ~ {Attributes:[{Name:"ascendant_mobs:amlevel_attribute",Base:50}]}
```
#### Minecraft 1.21+:

```mcfunction
/summon minecraft:sheep ~ ~ ~ {attributes:[{id:"ascendant_mobs:amlevel_attribute",base:50}]}
```
