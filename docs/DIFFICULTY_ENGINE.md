# Remnants: RPG Mobs: the difficulty engine

Design for the 3.1 line: a world difficulty that rises with time, distance, depth and player power and drives the
existing levels and ranks; event nights; player-side progression; and loot that scales with difficulty. Branch:
`claude/difficulty-engine`. Status: **design + Phase 1 in progress** (see [Status](#status) at the end).

The guiding rule: **extend what 3.0 already has instead of building a parallel system.** 3.0 already turns "how
dangerous is this place" into a level (the *threat*), and level already drives rank odds. The difficulty engine
is a richer threat formula, not a second number fighting with the first.

---

## 1. What the repo looks like today

| | |
| --- | --- |
| Layout | One MultiLoader workspace per Minecraft version (`1.20.1/`, `1.21.1/`, `26.1.2/`, `26.2/`, `26.3/`), each with `common/` + two loader subprojects. No Architectury; code is copied between workspaces and adjusted per version (the `common/` trees differ by ~400 diff lines between 1.20.1 and 1.21.1). |
| Loaders | 1.20.1: Fabric + Forge 47 (ModDevGradle legacyforge). 1.21.1, 26.x: Fabric + NeoForge. **There is no Forge 26.x workspace.** |
| Java | 1.20.1 targets Java 17 (`java_version=17`, toolchain); 1.21.1 Java 21; 26.x Java 25. |
| Mod id | `remnant_mobs` (3.0). 2.x was `rpgmoblevelingsystem`; 3.0 migrates its configs and leveled mobs and refuses to load next to it. |
| Config | Own commented-JSON loader (`config/CommentedJson.java`): typed readers, comments regenerated, missing keys restored, `/remnants reload`. Files: `levels.json`, `ranks.json`, `traits.json`, `rewards.json`, `client.json` in `config/remnant_mobs/`. |
| jauml | **Not used by 3.0 at all.** 2.0 dropped it ("Gson JSON config ... replaces Jauml on 1.20.1"); no build file, `mods.toml` or `fabric.mod.json` references it. See §9. |
| Tests | JUnit tests in `1.20.1/common/src/test` (config + mob data). In-game QA over RCON: `tools/qa/run_all.py`, `server_smoke.py`. |
| Assets | Generated from code by `tools/assets/build.py` (procedural pixel art, synthesised sounds): original by construction. |

### Existing systems the engine plugs into

| System | Where | What it does |
| --- | --- | --- |
| Threat / levels | `mob/Threat.java`, `levels.json` | `band.min + distance + days + depth + moon`, structure minimum, then pulled toward nearby players' level (RAS or XP) by `players.weight`, clamped to the dimension band. Mob level = threat ± `spread`. |
| Ranks | `MobLeveler.rollRank`, `ranks.json` | Common/Veteran/Elite/Champion/Remnant. Chance × `(1 + level / 50)` × spawner × Hunter's Moon multipliers. |
| Traits | `trait/TraitEffects.java`, `traits.json` | 16 traits, rolled by rank. |
| Remnants | `RemnantBars`, `Titles` | Remnant rank: generated title, boss bar (yellow), horn, kill broadcast. |
| Hunter's Moon | `event/HuntersMoon.java`, `levels.json → hunters_moon` | On a full moon, a 35% chance: title + bell + chat at dusk, +5 levels, ×2 rank odds, ×2 shards until dawn. State in `<world>/remnant_mobs.json`. `/remnants moon [start\|stop]`. **It does not spawn anything or target players.** |
| Rewards / Valor | `reward/Rewards.java`, `rewards.json` | XP × level × rank, extra loot rolls, Remnant Shards, RAS Valor. |
| Nameplates | `client/Plate*.java`, `client.json` | Rank crest, level colour, **health bar** in rank colour with damage trail, traits. Already the "mob health bar". |
| Hunter's Lens HUD | `client/LensHud.java` | While holding the lens: threat where you stand. Already a difficulty readout, gated behind an item. |
| Commands | `/remnants` (alias `/rml`) | `threat`, `inspect`, `spawn`, `set`, `moon`, `reload`. |
| Network | `mob_info`, `player_state` payloads | Optional on the client (Forge channels accept missing; Fabric checks `canSend`). |

---

## 2. Already there vs. new

| Ask | Already in 3.0 | New work |
| --- | --- | --- |
| 1. World difficulty from days, distance, depth, player power, per dimension | days, distance, depth, RAS/XP level, per-dimension bands, structure minimums | **time played** as an alternative/extra clock, **gear score**, **curves** (linear/sqrt/log/power + cap) per factor, **per-dimension multiplier/offset/factor overrides**, difficulty → **rank odds** curve, dimension exclude list incl. dimension-type tags |
| 2. Player progression | — | Heart growth item, two healing items, always-on difficulty meter (HUD + command), meter in `player_state` |
| 2. Mob health bars | Nameplates with health bars | Optional bar for unranked mobs at range; nothing else |
| 3. Elites readable in 1 s | Rank colour on plate, Remnant boss bar + title | **Server-side** aura particles per rank, optional glow in rank colour, boss bars for Champion (configurable), event titles. All work for vanilla clients |
| 4. Event nights | Hunter's Moon (buff-only) | **Event system** (`events.json`): triggers, announcement, effects, **hunters + mini-boss that track players**, rewards, boss-tier hook. The Hunter's Moon becomes one event of it with identical behaviour |
| 5. Loot scales with difficulty | Extra rolls per rank, shards | Extra rolls/shards scale with difficulty; optional Mob LootBags drops; loot beam |
| 6. Pack-maker controls | Commented JSON, entity tags (`bosses`, `never_leveled`, `never_ranked`), id/tag/namespace filters | `difficulty.json`, `events.json`, new entity tags, dimension-type tags, overlap detection |
| 7. Server-side first | Payloads optional | New features server-side; see §8 for the honest limit |
| 8. Backward compat | 2.x migration | Upgrade detection: new systems **off** when a 3.0 config exists |

---

## 3. The difficulty model

### 3.1 One number: difficulty is the threat

Difficulty is measured **in levels**, exactly like today's threat, so everything downstream (mob level, rank
odds, nameplate colours, rewards, RAS challenge) keeps working without translation. With the engine off
(`difficulty.json → enabled: false`) `Threat.at` runs the 3.0 formula unchanged. With it on:

```
place    = Σ factor_i                       (each factor: input → curve → cap, see 3.2)
           factors: days | played | distance | depth | gear
place    = (band.min + place) × dim.multiplier + dim.offset + event bonus
place    = max(place, structure minimum)
level    = place + (player level − place) × players.weight     (3.0 behaviour, RAS or XP)
level    = clamp(level, band.min, band.max)
```

- `days` and `distance` and `depth` default to the 3.0 linear values (`days_per_level 4, max 15`,
  `128 blocks/level`, `12 blocks/level below y 32, max 8`) so turning the engine on does not reshuffle a world;
  it **adds** `played` and `gear`.
- **Per-player inputs** (`played`, `gear`) are aggregated over players within `players.radius` with the existing
  `players.mode` (average / nearest / highest). No players nearby → 0.
- The **difficulty fraction** `f = (level − band.min) / (band.max − band.min)` (0…1) is what non-level systems
  use: rank odds, loot scaling, the meter's fill.

### 3.2 Curves

Every factor uses the same small shape, so pack makers learn it once:

```jsonc
"distance": { "enabled": true, "curve": "linear", "per": 128, "exponent": 1.0, "max": 40 }
```

`t = input / per`; levels = `linear: t`, `sqrt: √t`, `log: ln(1 + t)`, `power: t^exponent`; then `min(levels, max)`.
`max` ≤ 0 means uncapped. Inputs: days (overworld day count), played (hours of the player's own play time,
read from the vanilla `play_time` statistic, so no new save data), distance (overworld-scaled blocks from
spawn), depth (blocks below `start_y`), gear (gear score, 3.3).

### 3.3 Player power

- **Level**: the existing RAS / vanilla XP pull (`levels.json → players`). Unchanged.
- **Gear score** (new): `armor × 1 + toughness × 1.5 + max(0, attack_damage − 1) × 1 + max(0, max_health − 20) / 2`,
  read from the player's live attribute values (works for any modded gear, no item lists). Weights configurable.

### 3.4 Rank odds

The 3.0 `1 + level / level_chance_divisor` stays. With the engine on, rank chances are additionally multiplied by
`1 + (rank_odds.max_multiplier − 1) × curve(f)` (default max ×2 at full difficulty, `sqrt` curve), and by the
active event's `rank_multiplier`.

### 3.5 Per dimension

`difficulty.json → dimensions."<id>"`: `multiplier`, `offset`, and optional per-factor overrides (any key of a
factor). Defaults: Nether `multiplier 1.15`, End `1.25`. **Exclude** list: dimension ids or `#tag` of
`dimension_type` (datapack tag `data/<ns>/tags/dimension_type/…`; the mod ships
`#remnant_mobs:no_difficulty`, empty). Excluded dimensions use the 3.0 formula.

### 3.6 Entity controls (datapack tags)

| Tag | New? | Meaning |
| --- | --- | --- |
| `#remnant_mobs:never_leveled`, `never_ranked`, `bosses` | existing | unchanged |
| `#remnant_mobs:ignores_difficulty` | new | levels with the 3.0 formula even when the engine is on |
| `#remnant_mobs:event_hunters` | new | mobs an event may send after players (default: zombie, husk, skeleton, stray, spider, vindicator, pillager, witch) |
| `#remnant_mobs:event_mini_bosses` | new | mini-boss candidates (default: vindicator, wither skeleton, ravager… filtered by dimension) |

`dimension_type` tags: `#remnant_mobs:no_difficulty`, `#remnant_mobs:no_events`.

---

## 4. Event nights

### 4.1 What the Hunter's Moon does today

Buff-only full-moon night: chance roll once per full-moon night, announcement (title, sound, chat), +levels,
×rank odds, ×shards for mobs spawned while it is up, persistent across restarts, ends at dawn. No hunters, no
mini-boss, no rewards beyond more shards, nothing schedulable other than "full moon".

### 4.2 The event system

`events.json` holds a list of **event definitions**. The scheduler checks once a second; at most one event runs
at a time. Built-in definitions:

- `hunters_moon`: **is** the 3.0 Hunter's Moon. Its numbers stay in `levels.json → hunters_moon` (so existing
  configs keep working), the existing `HuntersMoon` class keeps its state file and logic. The event system only
  reports it (`/remnants event list`) and keeps other events from overlapping it.
- `grim_vigil` (new, original): every `every_days` nights (default 10, from day 7, chance 50%), a night where
  named elites and one mini-boss hunt each player.

```jsonc
"grim_vigil": {
  "enabled": true,
  "trigger": { "every_days": 10, "first_day": 7, "chance": 0.5, "min_difficulty": 0.0, "moon": "any" },
  "announce": { "title": true, "chat": true, "sound": true, "warn_seconds": 30 },
  "effects": { "level_bonus": 3, "rank_multiplier": 1.5, "shard_multiplier": 1.5 },
  "hunters": { "waves": 3, "per_wave": 3, "wave_seconds": 90, "min_rank": "veteran", "named": true,
               "distance_min": 24, "distance_max": 40, "max_alive": 6 },
  "mini_boss": { "enabled": true, "wave": 3, "rank": "remnant", "level_bonus": 5, "tier": "mini" },
  "rewards": { "survive_experience": 50, "boss_shards": 8, "advancement": true },
  "dimensions": ["minecraft:overworld"]
}
```

- **Hunters** are picked from `#remnant_mobs:event_hunters`, placed on valid spawn spots 24–40 blocks from the
  player (vanilla spawn placement rules, never in view, never in protected spawn radius), given the event's
  minimum rank and a generated title (the existing `Titles` generator), set to target and path to the player,
  and tagged `remnant_mobs.event` (scoreboard tag, so datapacks can react).
- The **mini-boss** arrives on the last wave with a Remnant-rank roll → it gets the existing boss bar, horn and
  broadcast for free.
- **Rewards**: surviving to dawn grants XP (and Valor through the existing RAS bridge if present); the mini-boss
  drops `boss_shards` extra shards. Event mobs multiply shards by `shard_multiplier`.
- **Boss-tier hook**: `mini_boss.tier` names an entry in a `BossTiers` registry (`api/BossTier`). Phase 1 ships
  `mini` (a ranked vanilla mob). Remnant Bosses or a later phase can register `major`, `raid` etc. through
  `RemnantMobsApi.registerBossTier(id, spawner)` without changing the event format.
- Commands: `/remnants event` (status), `/remnants event list`, `/remnants event start <id>` (op),
  `/remnants event stop` (op). `/remnants moon …` is unchanged.
- State persists in `<world>/remnant_mobs_events.json` (separate from the Hunter's Moon file so 3.0 worlds
  load untouched).

Clip value for Shorts/Reels: red title card, horn, the named hunters' boss bar, and a server-wide line when the
mini-boss falls. All of it is vanilla packets, so it records the same on a vanilla client.

---

## 5. Player-side progression (Phase 2)

All original names and art, generated by `tools/assets`.

| Item | Effect | Source |
| --- | --- | --- |
| **Vital Ember** | +1 heart max health, permanent (attribute modifier `remnant_mobs:vital_growth`), cap `+10 hearts` (config), kept on death (config) | Champion+ drops (chance scales with difficulty), event mini-boss, recipe: 4 Remnant Shards + 4 gold + 1 golden apple |
| **Mending Salve** | Heals 6 HP over 3 s, 10 s cooldown | Shards + honey bottle + glow berries |
| **Hunter's Draught** | Heals 40% + 5 s regeneration I, 30 s cooldown, stack 4 | Elite+ drops, event rewards |

- **Difficulty meter**: `/remnants difficulty [player]` (breakdown per factor) works for everyone, vanilla clients
  included. Clients with the mod get an optional HUD gauge (`client.json → hud.difficulty`, default on,
  positionable), fed by a **new optional payload** `remnant_mobs:difficulty` (separate from `player_state` so
  3.0 clients joining a 3.1 server do not mis-decode).
- **Health bars**: the existing plates. Phase 2 adds `nameplates.bar_only_distance` (a slim bar for Common mobs
  further out) — nothing else is needed.

## 6. Elites readable in one second (Phase 2)

Server-side, so it reaches vanilla clients:

- **Aura**: rank-coloured dust particles around Elite+ (Champion: ring, Remnant: ring + soul embers), sent every
  10 ticks to players within 24 blocks. `ranks.json → readability.aura`.
- **Glow** (off by default): scoreboard team per rank, team colour = nearest chat colour of the rank; makes the
  outline readable through walls during event nights only (`events` effect `glow: true`).
- **Title**: Champion-rank mobs optionally get titles too (`remnant.titles_from_rank`).
- **Bar colour**: Remnant boss bar colour configurable; Champion bars optional (`PURPLE`), event hunters `RED`.

## 7. Loot that scales (Phase 3)

- Extra loot rolls and shard counts × `1 + loot.difficulty_bonus × f`.
- **Mob LootBags** (soft dependency, detected by mod id at runtime, no compile dependency): ranked kills may drop a
  bag; rank → tier map in `rewards.json → loot_bags` (item ids, so no API coupling). Off when the mod is absent.
- **Loot beam**: server-side particle column above dropped items whose rarity ≥ `rare` or that came from a
  Champion+ / event kill, for up to 30 s. Cheap: one particle packet per item per 10 ticks, capped count.

## 8. Server-side first: the honest limit

- Everything Phase 1 adds (difficulty, events, commands, hunters, titles, boss bars, particles) is server-side
  and uses vanilla packets.
- **But 3.0 already registers items (Remnant Shard, Hunter's Lens, Brand of Challenge) and sounds.** On Forge and
  NeoForge, registry sync means a vanilla client cannot join a server with registered items; on Fabric the
  client would get unknown item ids. So today the mod is a both-sides mod and the metadata says so correctly
  (`environment: "*"`, Forge `side = "BOTH"`, NeoForge `side = "BOTH"`).
- A truly vanilla-joinable build would need a **"server edition"** jar without registered content (shards become
  tagged vanilla items, sounds become vanilla sounds). That is a product decision → open question Q3. The new
  code is written so that every Phase 1 feature works with no new registry entries and no required payloads.

## 9. jauml bundling

**Finding: 3.0 does not depend on jauml.** Modpacks updating from 2.x→3.x no longer need it for this mod.
Recommendation: do **not** add it back. If the studio wants a shared library for the other ~13 mods:

| Loader | Jar-in-jar | Risk for the other mods |
| --- | --- | --- |
| Fabric | `include(modImplementation(...))` | Loader picks the highest nested version across all mods; safe if jauml stays backward compatible. |
| Forge 1.20.1 (JarJar) | `jarJar(...)` with a range `[x,)` | Forge picks one version satisfying every declared range; a mod pinning `[x]` exactly breaks resolution. Use open ranges in every mod. |
| NeoForge | `jarJar(...)` same model | Same; plus the standalone jauml jar in a pack must have the same mod id/version scheme or both load. |

Rule if bundling later: open version ranges everywhere, never change the mod id, keep publishing the standalone jar.

## 10. Coexistence with other elite-mob mods

`difficulty.json → compat.overlap_mod_ids`: mod ids of affix/elite mods that overlap our traits. When one is
loaded and `compat.disable_overlaps` is true, **trait rolls are turned off** (ranks, levels, events stay — they
do not overlap) and a log line says why. Default ids: the two most widespread elite-affix mods (see Q6). On
upgraded installs `disable_overlaps` defaults to `false` (packs keep current behaviour).

## 11. Config schema

New files only — **no new keys are injected into the five 3.0 files**, so their text stays byte-identical:

- `config/remnant_mobs/difficulty.json`: `enabled`, `factors` (days, played, distance, depth, gear),
  `gear_weights`, `rank_odds`, `dimensions`, `exclude_dimensions`, `compat`.
- `config/remnant_mobs/events.json`: `enabled`, `check_seconds`, `events.<id>` as in §4.2.
- Phase 2: `progression.json` (heart growth, healing, meter defaults).

### Migration defaults (requirement 8)

On load, for each **new** file that does not exist yet:

| Situation | Detected by | New systems |
| --- | --- | --- |
| Fresh install | no `config/remnant_mobs/` *and* no `config/rpgmoblevelingsystem/` | **on** |
| Upgrade from 3.0 | `config/remnant_mobs/levels.json` exists | **off** (`enabled: false`, `compat.disable_overlaps: false`) |
| Upgrade from 2.x | `config/rpgmoblevelingsystem/` exists | **off** |

Once written, the file is the user's; the rule never runs again. The decision is logged and written into the
file header ("created as an upgrade: new systems start off"). Worlds: no change to the mob data tag format, no
change to `remnant_mobs.json`; event state goes to a new file.

### Migration note (for the changelog)

> **Upgrading from 3.0 or 2.x?** Nothing changes until you opt in. Remnants finds your existing config and
> creates `difficulty.json` and `events.json` with every new system switched off. Set `enabled: true` in each to
> try them; `/remnants reload` applies it. Fresh installs start with them on. The Hunter's Moon, levels, ranks,
> traits and rewards behave exactly as before either way.

## 12. Minecraft 26.4 overlap

26.4 (Q4 2026) adds a Freezing effect and a frost zombie. Plan:

- No new frost mechanics anywhere in this engine.
- The 26.x port of the **Frostbite** trait looks up `minecraft:freezing` at runtime; if present it applies the
  vanilla effect instead of Slowness + freeze ticks; otherwise 3.0 behaviour.
- The frost zombie joins `#remnant_mobs:event_hunters` in the 26.4 workspace (via an optional tag entry
  `{"id": "...", "required": false}` so the same data works before 26.4).

## 13. Phases

| Phase | Scope | Targets |
| --- | --- | --- |
| **1** | Difficulty engine (factors, curves, dimensions, rank odds), `difficulty.json` + `events.json` with migration defaults, `/remnants difficulty`, `/remnants event …`, event system with `grim_vigil` + Hunter's Moon adapter, new tags, unit tests | 1.20.1 (Forge first, Fabric shares `common/`), then 1.21.1 (NeoForge, Fabric) |
| **2** | Vital Ember, healing items, difficulty payload + HUD gauge, elite readability (aura, bars, titles), overlap detection polish | same |
| **3** | Loot scaling, Mob LootBags soft dep, loot beam | same |
| **4** | Port to 26.1.2, 26.3 (Fabric, NeoForge), 26.2 if still shipped; 26.4 freezing hook | 26.x |
| **5** | Forge 26.x: needs a new workspace (none exists today) | — |
| later | KubeJS hooks, more boss tiers | — |

## 14. Open questions for the owner

1. **Mod id / project.** 3.0 already changed the mod id from `rpgmoblevelingsystem` to `remnant_mobs` and the
   `fabric.mod.json` homepage still points to `rpg-mob-leveling-system` on Modrinth. Has 3.0 been published to the
   `remnants-rpg-mobs` projects? The 143 packs on 2.x will see a new mod id on update no matter what this branch
   does; 3.0's 2.x config/mob migration covers worlds, but pack scripts referencing `rpgmoblevelingsystem` will not.
2. **Clock for difficulty.** Default: world days (as 3.0) **plus** each player's own play time at a smaller
   weight. Should difficulty ever go **down** (on death, with an item)? Not in the design today.
3. **Server edition.** Do you want a vanilla-client-joinable jar without registered items (§8)?
4. **Event cadence.** `grim_vigil` every 10 nights, 50% chance, from day 7: too much or too little for packs?
   Should it refuse to start while a player is in a bed / AFK?
5. **Names.** Proposed original names: *Grim Vigil* (event), *Vital Ember*, *Mending Salve*, *Hunter's Draught*.
   OK, or a different naming family?
6. **Overlap detection.** The rules say not to name other mods anywhere, but detecting them needs their mod ids.
   The design keeps the two ids only as default values of `compat.overlap_mod_ids` (a config value, never shown in
   docs or descriptions). Accept, or ship the list empty and let pack makers fill it?
7. **Mob LootBags item ids** per tier (needed for Phase 3; that repo is not in this session's scope).
8. **Java for 1.20.1.** The workspace targets Java 17 (the task brief said 21). Keep 17 (wider compatibility)?

## Status

_Filled in as work lands; see the bottom of this file._
