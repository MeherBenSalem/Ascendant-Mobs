=============================================
## Changelog 1.3.0
=============================================

* Replaced the old level name HUD with a new HUD system, now separate from the vanilla display name.
* Added new configuration options:
    * `enable_name_render` to show the mobs health
    * `display_lvl_y_offset` the y offset of the mobs name
    * `level_x_offset_level` the x offset of the mobs level
    * `level_y_offset_level` the y offset of the mobs level
    * `level_color_code` the color of the mobs level
* Fixed compatibility issues with **Alex's Mobs** mod.

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
