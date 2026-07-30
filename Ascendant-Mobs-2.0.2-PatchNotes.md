# Ascendant-Mobs v2.0.2

### Bug Fixes
* Fixed NeoForge/Fabric crash when `mobs_list_settings.json` is missing or partial: `mobs().banned` (and related lists/maps) are now normalized to empty defaults on load instead of staying null (fixes [#18](https://github.com/MeherBenSalem/Ascendant-Mobs/issues/18)).

### Compatibility
* Shared version **2.0.2** across 1.20.1 and 1.21.1 workspaces.
* Drop-in update from 2.0.1.

### Upgrade Notes
1. Replace old jars with the matching `rpgmoblevelingsystem-*-2.0.2.jar` for your loader and Minecraft version.
2. Restart the client or server.
3. Existing `config/rpgmoblevelingsystem/mobs_list_settings.json` files are auto-repaired on startup if any list fields were null.
