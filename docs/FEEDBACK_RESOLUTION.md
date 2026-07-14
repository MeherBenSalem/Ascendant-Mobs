# Feedback Resolution Report — v2.0.0

| Report | Root cause | Fix | Test | Status |
|--------|------------|-----|------|--------|
| Low-level mobs too strong vs RAS players | Flat additive scaling ignored player power | `playerBalanceEnabled` + RAS combat snapshot ratios | Config + manual | Fixed |
| Percentage attribute scaling | Only additive base mutation existed | `mode: percent` in attribute rules + ADD_VALUE modifiers | Unit rounding tests | Fixed |
| Movement speed corruption | `setBaseValue` on passives | Named modifiers; `hostileOnly` on speed rule | Build + manual | Fixed |
| Hostile-only scaling | All living entities scaled | `hostileOnly` config + `EntityClassification` | Manual | Fixed |
| Exclude namespaces (Cobblemon) | Per-entity ban only | `banned_namespaces` list | Unit config | Fixed |
| Structure min / boss locks | Locked list only | `structureMinLevels`, `bossLevelLocks` | Manual | Fixed |
| Custom loot by level | Not implemented | `loot_by_level.json` + death handler | Manual | Fixed |
| Zero XP modded dimensions | Level tag missing on XP calc | XP uses stored/preview level; Fabric mixin | Build | Fixed |
| Locked mobs wrong HP forever | Stacked `setBaseValue` | Modifiers + heal on apply | Manual | Fixed |
| Ender Dragon scaling | Wrong formula / no lock | `bossLevelLocks` default + locked path | Unit | Fixed |
| Modded bosses | No generic boss path | Boss locks + `rpgmoblevelingsystem:boss` tag | Manual | Fixed |
| Overlay console spam | Debug `LOG.info` every 80 ticks | Removed; `overlayDebug` flag | Build | Fixed |
| Lag in large packs | Re-application / logging | One-time apply; early namespace skip | Build | Fixed |
| NeoForge/Fabric crashes | 1.21.1 partial port | Full unified services; builds green | Gradle build | Fixed |
| Missing Tome textures | Item registered as `calender` | Existing calendar model retained | Build | Verified |
| Documentation | Template README only | `docs/USAGE.md`, `docs/API.md` | N/A | Fixed |
| Jade name replacement | Client overwrote custom name | No `setCustomName` in overlay; tag-based level | Manual | Fixed |
| Distance scaling bug | Squared distance divisor | Euclidean distance / scaleDistance | Unit | Fixed |
| Day scaling | `dayTime` vs total days | `getGameTime() / 24000` | Unit | Fixed |
