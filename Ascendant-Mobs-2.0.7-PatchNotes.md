# RPG Mob Leveling System — 2.0.7

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge)

### Features / Compatibility
* **Neat soft integration** — when [Neat](https://modrinth.com/mod/neat) is installed, mob levels appear on Neat unit-frame name plates using `nameFormat` (default `%mob_name% [Lv. %level%]`). Neat remains fully optional.
* **Legacy HUD auto-disable** — floating level digits are suppressed while Neat is present to avoid double UI.

### Bug Fixes / Polish
* **Oversized legacy level digits** — fallback overlay scale reduced to nametag-sized text when Neat is absent.
* **NeoForge / Fabric 1.21.1 missing overlays** — fixed incorrect partial-tick source (`getGameTimeDeltaTicks` → `getGameTimeDeltaPartialTick`) so level labels lerp and render correctly.

### Upgrade Notes
1. Replace previous jars with the matching 2.0.7 loader jar.
2. Optional: install Neat for polished plates; no config changes required.
3. Without Neat, legacy number HUD still works (now smaller).
