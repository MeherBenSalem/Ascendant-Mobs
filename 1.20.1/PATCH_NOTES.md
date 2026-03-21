## RPG Mob Leveling System 1.5.0 - Patch Notes

### ✅ What’s New
- Unified mob level display for both Fabric and Forge using a clean overlay (no more name tag clutter).
- Mob levels now show as number only (e.g., `10`), with low-level values in green and high-level values in red.
- Added configuration options to fine-tune overlay position:
  - `overlay_x_offset`
  - `overlay_y_offset`
  - `overlay_z_offset`
- All legacy `Ascendant Mobs` references replaced with `RPG Mob Leveling System` and internal mod ID set to `rpgmoblevelingsystem`.

### 🛠️ Behavior changes
- Mob display names are no longer overwritten for level display.
- Level tracking is now driven by a secure tag (`am_level_*`) and rendered client-side.
- Users can enable/disable HUD overlays via config (`use_legacy_hud`).

### 🚀 Stability
- Build verified successful for common, Fabric, and Forge modules.
- All remaining legacy ID and package issues resolved.

### 💡 Notes for players
- Calendar item still works and provides world/time info in chat.
- Level scaling and rewards behavior remains the same as prior system logic.
