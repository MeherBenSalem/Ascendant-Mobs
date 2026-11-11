# Remnants: RPG Mobs: configuration reference

Every option in every config file the mod writes, what it does in game, its default and the values it accepts.
It applies to all five workspaces (1.20.1, 1.21.1, 26.1.2, 26.2, 26.3) and every loader (Fabric, Forge, NeoForge):
the config code is the same in all of them (see [Version differences](#version-differences)).

`ConfigDocsTest` in each workspace's `common` tests fails the build when a key in the code is missing here, when a key
here no longer exists, or when a single default listed here no longer matches the file the mod writes.

## Contents

- [How difficulty works](#how-difficulty-works)
- [Which keys control how strong mobs get](#which-keys-control-how-strong-mobs-get)
- [Common recipes](#common-recipes)
- [How the files work](#how-the-files-work)
- [The settings screen](#the-settings-screen)
- [Reference](#reference)
  - [`levels.json`](#levelsjson): levels, bands, distance/time/depth, player scaling, what a level does, Hunter's Moon
  - [`ranks.json`](#ranksjson): rank chances and rank stats
  - [`traits.json`](#traitsjson): the 16 traits
  - [`rewards.json`](#rewardsjson): experience, loot, shards, Valor
  - [`difficulty.json`](#difficultyjson): the difficulty engine
  - [`events.json`](#eventsjson): event nights
  - [`progression.json`](#progressionjson): Vital Embers, healing items, the difficulty meter
  - [`elites.json`](#elitesjson): auras, boss bars, titles, glow
  - [`loot.json`](#lootjson): loot scaling, Mob LootBags, loot beams
  - [`client.json`](#clientjson): nameplates and HUD
- [Version differences](#version-differences)

## How difficulty works

### The meter in the bottom right

With the mod on your client, a small gauge sits in the bottom right corner: a skull, a percentage and a bar that turns
from green (below 25%) through gold and orange to deep red (75% and up), with a red "+N" on Hunter's Moon and event
nights. This is the **difficulty meter**.

The percentage is where the **threat** of the spot you stand on sits inside your dimension's level band:

```
meter % = (threat level here - band min) / (band max - band min) x 100
```

The threat level is the level a monster spawning where you stand would get right now, before its own random spread:
the Overworld band is 1-60, so a threat of 30 reads 49%. "+N" is the levels a Hunter's Moon (`levels.json →
hunters_moon.level_bonus`) and a running event night (`events.json → effects.level_bonus`) add tonight. The server
works it out once a second and only sends it when it changes. `/remnants difficulty` prints the same number factor by
factor and works for every player, with or without the mod.

The meter needs `progression.json → enabled` and `meter.send_to_players` on the server, and `meter.hud` on your
client (see [Hide the difficulty meter](#hide-the-difficulty-meter)). It hides with the rest of the HUD (F1).

### What makes it rise

The threat of a place is built from these parts (with the difficulty engine on, the default on new installs):

```
threat = (band min + distance + days + depth + played + gear) x dimension multiplier + dimension offset
       + Hunter's Moon bonus + event night bonus
threat = at least the structure minimum                (ancient city 40, bastion 30, ...)
threat = threat + (players' level - threat) x players.weight     (players within players.radius)
level  = the threat rounded down, kept inside the dimension's band
```

| Part | Grows with | Keys (engine on) | Keys (engine off) |
| --- | --- | --- | --- |
| distance | horizontal blocks from world spawn (a Nether block counts as 8) | `difficulty.json → factors.distance` | `levels.json → distance` |
| days | in-game days of the world | `difficulty.json → factors.days` | `levels.json → time` |
| depth | blocks below Y 32, in dimensions with a sky | `difficulty.json → factors.depth` | `levels.json → depth` |
| played | hours the nearby players have played | `difficulty.json → factors.played` | (none) |
| gear | the nearby players' armor, toughness, attack damage and extra health | `difficulty.json → factors.gear`, `gear_score` | (none) |
| dimension | which dimension you are in | `levels.json → dimensions`, `difficulty.json → dimensions` | `levels.json → dimensions` |
| structures | standing inside a listed structure | `levels.json → structures` | the same |
| players | the RPG Attribute System (or experience) level of players nearby | `levels.json → players` | the same |
| nights | a Hunter's Moon or an event night | `levels.json → hunters_moon`, `events.json` | the same |

So the meter rises as the world ages, as you travel away from spawn or dig deep, as you play longer and gear up, and
on dangerous nights. It falls when you walk back toward spawn or take gear off. World days and play time never go
down. With RPG Attribute System installed, nearby players' RAS levels pull the threat toward their own level (35% of
the way by default), up or down.

`difficulty.json → enabled` decides which formula runs. **New installs start with the engine on; installs that
already had a 3.0 or 2.x config start with it off** (3.0 formula: distance, days and depth from `levels.json`, no play
time or gear, no dimension multiplier). Either way the meter, levels and ranks work.

### From difficulty to mobs

- **Level.** A hostile mob gets its level once, the first time it ticks: the threat of its spawn point, plus or minus
  `levels.json → spread` (2), kept inside the band. `mobs.level_locks` fixes the level of a mob type instead.
- **Stats.** Each level adds attribute modifiers (`levels.json → attributes`): +6% max health, +3% attack damage,
  +0.15 armor and more per level by default. A level 30 zombie has 56 health and hits for 5.7.
- **Rank.** Every leveled hostile mob rolls a rank, rarest first. Each rank's chance is multiplied by
  `1 + level / 50`, by the difficulty engine's rank odds (up to x2 at 100% on the meter), by x2 under a Hunter's Moon
  and x1.5 on an event night, and by x0.25 for mobs from spawners. Ranks multiply health and damage and add armor:
  a level 30 Elite zombie has 112 health and hits for 7.1.
- **Traits.** A ranked mob rolls 1 (Veteran) to 4 (Remnant) traits.
- **Rewards.** Experience is multiplied by `1 + level x 0.04` and by the rank. Ranked mobs roll extra loot and drop
  Remnant Shards. With `loot.json` and `progression.json` on, loot, shards and item drop chances grow with where the
  **mob's** level sits in its band (the same 0-100% scale as the meter). With RPG Attribute System, the killer of a
  ranked mob gets bonus **Valor**: `(rank bonus + level) x challenge`, where challenge compares the mob's level with
  your RAS level (up to x1.5 for a mob 10 levels above you, down to x0.25 at 15 below). A level 30 Elite pays 69
  Valor to a level 25 hunter and 14 to a level 45 one.

## Which keys control how strong mobs get

There is no single "scaling strength" key. Mob strength is **level x what one level does x rank**, and each part has
its own keys. The table names them all; the reference below gives defaults and ranges.

| What you want to change | Engine on (`difficulty.json → enabled: true`, new installs) | Engine off (upgraded installs) |
| --- | --- | --- |
| Levels per block of distance | `difficulty.json → factors.distance.per` (128 blocks per level), `curve`, `max` | `levels.json → distance.blocks_per_level` (128) |
| Levels per in-game day | `difficulty.json → factors.days.per` (4 days per level), `max` (15) | `levels.json → time.days_per_level` (4), `time.max_levels` (15) |
| Levels per block of depth | `difficulty.json → factors.depth.per` (12), `max` (8), `start_y` (32) | `levels.json → depth.blocks_per_level`, `max_levels`, `start_y` |
| Levels from play time and gear | `difficulty.json → factors.played`, `factors.gear`, `gear_score` | (not used) |
| Per-dimension strength | `levels.json → dimensions.<dimension>.min/max/distance_scale` and `difficulty.json → dimensions.<dimension>.multiplier/offset` | `levels.json → dimensions.<dimension>` |
| Highest and lowest level | `levels.json → dimensions.<dimension>.max` and `.min`, `default_band` | the same |
| Health, damage, armor... per level | `levels.json → attributes[]`: `per_level` and `max` of each rule (max health +6%/level up to +600%, attack damage +3%/level up to +300%) | the same |
| How players pull levels | `levels.json → players.weight` (0.35), `source`, `mode`, `radius` | the same |
| Rank odds | `ranks.json → ranks.<rank>.chance`, `min_level`, `level_chance_divisor`, `spawner_multiplier`; `difficulty.json → rank_odds` | `ranks.json` only |
| Rank strength | `ranks.json → ranks.<rank>.health`, `damage`, `armor`, `traits` | the same |
| Trait strength | `traits.json → <trait>.<number>` (e.g. `vampiric.heal_fraction`) | the same |
| Dangerous nights | `levels.json → hunters_moon.*`, `events.json → events.<event>.effects.*` | the same |

**The usual trap:** with the engine on, `levels.json → distance`, `time` and `depth` no longer do anything. They are
only copied into `difficulty.json` once, when that file is first created. Change `difficulty.json → factors` instead,
or set `difficulty.json → enabled: false` to go back to the `levels.json` numbers.

"Scaling strength" in support requests usually means one of two things:

- *Mobs level up too slowly or too fast*: the distance/days/depth/played/gear keys above.
- *A level does too little or too much*: `levels.json → attributes[].per_level`. Doubling the `max_health` rule's
  `per_level` from 6 to 12 doubles the health a level adds (raise its `max` from 600 too, or the cap is reached at
  level 50 instead of 100).

## Common recipes

All snippets are partial: put the keys in the existing file and leave the rest. Missing keys are filled in from the
defaults when the file loads. Run `/remnants reload` (operators) or restart. New mobs use the new numbers; mobs that
already spawned keep their level until `/remnants set <targets> reroll`.

### Make mobs scale slower

Engine on (`difficulty.json`):

```jsonc
"factors": {
  "distance": { "per": 256 },              // one level every 256 blocks instead of 128
  "days": { "per": 8, "max": 10 },          // one level every 8 days, at most 10 instead of 15
  "played": { "enabled": false },           // play time adds nothing
  "gear": { "per": 12 }                     // half as many levels from gear
}
```

Engine off (`levels.json`): `"distance": { "blocks_per_level": 256 }`, `"time": { "days_per_level": 8, "max_levels": 10 }`.

To keep levels but make each level weaker, lower the rules in `levels.json → attributes`, for example the
`minecraft:max_health` rule's `per_level` from 6 to 3 and `minecraft:attack_damage` from 3 to 1.5.

### Make mobs scale faster

```jsonc
// difficulty.json
"factors": {
  "distance": { "per": 64 },               // one level every 64 blocks
  "days": { "per": 2, "max": 30 }           // one level every 2 days, up to 30
},
"dimensions": {
  "minecraft:overworld": { "multiplier": 1.2, "offset": 0, "factors": {} }
}
```

Raise `ranks.json → ranks.<rank>.chance` or lower `level_chance_divisor` (50) for more ranked mobs, and raise
`rank_odds.max_multiplier` (2) in `difficulty.json` for more of them deep in the band.

### Cap levels

- The hard cap is the band: `levels.json → dimensions."minecraft:overworld".max` (60), Nether 80, End 100, and
  `default_band.max` for every dimension not listed. No mob of that dimension goes above it (except
  `mobs.level_locks`).
- Cap one source: `difficulty.json → factors.distance.max` (0 = no cap by default), `factors.days.max` (15),
  `factors.depth.max` (8), `factors.played.max` (10), `factors.gear.max` (15).
- Cap what levels do: each rule in `levels.json → attributes` has a `max` (+600% health, +300% damage...).
- Fix a mob type's level: `"mobs": { "level_locks": { "minecraft:warden": 40 } }`.

### Hide the difficulty meter

- **Just for you:** in the settings screen, **Meter, nameplates and HUD → Show the meter (client)**, then Save; it applies
  at once, on any server. Without the screen: set `meter.hud` to `false` in **your own**
  `config/remnant_mobs/progression.json`, on the computer you play on (also when you play on a server), then restart the
  game. In single player `/remnants reload` applies it too.
  The meter's place is in the same section: `anchor` (`top_left`, `top_right`, `bottom_left`, `bottom_right`), `x`, `y`.
- **For every player on a server:** set `meter.send_to_players` to `false` in the server's `progression.json` and run
  `/remnants reload`. Turning `progression.json → enabled` off also stops it, but it stops the item drops too.
- The moon icon next to it is `client.json → hud.hunters_moon`.

### Make the early game easier

- **A slow start that ends in the same place**: square the days factor, so day 60 is still 15 levels but day 8 is 0.3
  instead of 2, and day 20 is 1.7 instead of 5:

  ```jsonc
  // difficulty.json
  "factors": { "days": { "curve": "power", "per": 15.5, "exponent": 2, "max": 15 } }
  ```

  The same works for distance: `"distance": { "curve": "power", "per": 320, "exponent": 1.5 }` gives 1 level at 320
  blocks (2.5 before), 5.5 at 1,000 (7.8) and the same 15.6 at 2,000, then climbs faster (44 at 4,000, against 31).
  Pair it with the band's `max` if you do not want that.
- **No levels from play time or gear early on**: `"played": { "enabled": false }` and `"gear": { "enabled": false }`,
  or make them slower (`per`). Full iron armor and an iron sword score about 20, 3 levels at the default 1 per 6.
- **No ranks in the first levels**: raise `ranks.json → ranks.veteran.min_level` (1), `ranks.elite.min_level` (8) and
  so on. A rank never rolls below its `min_level`.
- **Later event nights**: `events.json → events.grim_vigil.trigger.first_day` (7), or `trigger.min_difficulty` (for
  example `0.25`: only when a player stands at 25% or more on the meter). Rarer Hunter's Moons:
  `levels.json → hunters_moon.chance` (0.35).
- **With RPG Attribute System**: raise `levels.json → players.weight` (0.35 → 0.6) so mobs follow the players' RAS
  level more closely. It works both ways: strong players also pull weak places up.

## How the files work

- **Where.** `config/remnant_mobs/` next to the game (client) or the server. Ten files, written on first start with
  comments above every key.
- **Loading.** Each file is read leniently (`//` comments allowed) and merged over the defaults: keys you leave out come
  back with their default, keys the mod does not know are kept, and the file is written back with fresh comments when
  its text changed. If a file is not valid JSON, the mod uses the defaults for that file and leaves it untouched (see
  the log).
- **Bad values.** A value of the wrong type (text where a number belongs) falls back to the default. A number outside
  the range listed below is clamped when it is read; the file keeps what you wrote. Whole numbers are rounded
  (`2.6` reads as 3).
- **Applying changes.** `/remnants reload` (permission level 2) reloads every file on the server; saving in the
  [settings screen](#the-settings-screen) does the same. New mobs use the new
  numbers; mobs that already spawned keep their level, rank and traits until `/remnants set <targets> reroll`.
- **Server or client.** Everything except the client keys below is read by the server (the integrated server in single
  player), so on a multiplayer server only the server's files count. `client.json` and the `meter.hud`, `meter.anchor`,
  `meter.x` and `meter.y` keys of `progression.json` are read by each player's own game from their own copy, when the
  game starts. The Hunter's Lens stat sheet also shows the reward multipliers from the client's own `ranks.json` and
  `rewards.json`.
- **New in 3.1.** `difficulty.json`, `events.json`, `progression.json`, `elites.json` and `loot.json`: when one of them
  is first created next to an existing 3.0 or 2.x config, its `enabled` key starts `false` (and
  `difficulty.json → compat.disable_overlaps` too), so upgraded worlds behave as before. Fresh installs start with
  them on. The rule only runs when the file is created.
- **Lists of mobs** (`mobs.exclude`, trait `exclude`) take entity ids (`minecraft:creeper`), entity type tags
  (`#minecraft:skeletons`) and whole namespaces (`cobblemon:`). **Lists of dimensions** take dimension ids and
  `#` tags of dimension types.
- **Ranks** are written `veteran`, `elite`, `champion`, `remnant` (and `common` where it is allowed).
- **Placeholders.** In the tables below, `<dimension>`, `<structure>`, `<rank>`, `<trait>`, `<factor>` and `<event>`
  stand for any one entry of that map.

## The settings screen

Since 3.2 the difficulty and scaling settings can be changed in game. Open it with:

- **Fabric:** Mod Menu's config button for Remnants (Mod Menu is optional; without it, use the command);
- **NeoForge and Forge:** the **Config** button of Remnants in the Mods list (title screen or pause menu);
- **any loader, in a world:** `/remnants config`.

Eight sections: difficulty engine, levels and bands, health and damage per level, ranks, traits, rewards, Hunter's Moon
and event nights, and the meter, nameplates and HUD. Each option shows its name and its description from this page,
whose setting it is, its default, its range and the file and key it writes; it has a reset button. Numbers with a range
at both ends get a slider, other numbers a field, switches and choices a button.

The JSON files stay the only source of truth. The screen reads them and writes them through the same code the mod
loads them with: comments and keys it does not show are kept, and a value is only written as the mod would read it
(clamped to the ranges below; a file that is not valid JSON is never overwritten). Saving reloads like
`/remnants reload`.

| Where you are | Server settings | Your own settings (meter, nameplates, HUD) |
| --- | --- | --- |
| Title screen (no world) | this computer's files, which single-player worlds use | this computer's files |
| Single player, or a LAN world you host | editable: the world's server writes them and reloads | editable |
| A server, as an operator (permission level 2, like `/remnants reload`) | editable: the server checks your permission and every value again, writes, reloads and updates every player | editable, saved on your computer |
| A server, not an operator | read-only (the server refuses changes even if a modified client sends them) | editable, saved on your computer |
| A server without Remnants 3.2 | not shown | editable, saved on your computer |

Not in the screen (edit the files for those): lists (excluded mobs, dimensions, mod ids), `elites.json`, loot bags and
loot beams, boss bar colours, healing items and hearts, and the rest of an event night's settings (announcements,
hunter spawn distances, mini-boss, rewards).

## Reference

Columns: the key (dots are nesting: `distance.blocks_per_level` is `"distance": { "blocks_per_level": ... }`), its type,
its default, the values it accepts (what the loader clamps to) and what it does. "Fresh / upgrade" defaults depend on
whether the file was created on a fresh install or next to an older config.

### `levels.json`

How levels are worked out and what they do to a mob. Read by the server.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` | | **Leveling.** Master switch. When false, newly spawned mobs get no level, rank or traits (mobs already leveled keep theirs; event night hunters are still leveled). |
| `affect_passive` | boolean | `false` | | **Level passive mobs.** Also level passive and neutral creatures (animals, villagers...). They never get ranks or traits. |
| `default_band.min` | integer | `1` | 0 and up | **Default lowest level.** Lowest level in dimensions not listed under `dimensions`. |
| `default_band.max` | integer | `60` | at least `min` | **Default highest level.** Highest level in dimensions not listed under `dimensions`. |
| `default_band.distance_scale` | number | `1.0` | 0 and up | **Default distance scale.** Multiplies the distance part of the threat in unlisted dimensions (above 1 faster, below 1 slower). |
| `dimensions.<dimension>.min` | integer | Overworld `1`, Nether `15`, End `35`, `remnant_bosses:eclipsed_realm` `30` | 0 and up | **Lowest level.** The bottom of the dimension's band: every mob there is at least this level, and the threat starts here. A key left out of an entry falls back to `default_band`. |
| `dimensions.<dimension>.max` | integer | Overworld `60`, Nether `80`, End `100`, `remnant_bosses:eclipsed_realm` `100` | at least `min` | **Highest level.** The top of the band: the level cap for the dimension, and 100% on the meter. |
| `dimensions.<dimension>.distance_scale` | number | `1.0` for all four | 0 and up | **Distance scale.** Multiplies distance levels in this dimension (engine off) or the distance fed into `factors.distance` (engine on). |
| `distance.blocks_per_level` | number | `128` | 1 and up | **Blocks per level.** Engine off: one level per this many blocks from world spawn, horizontally, counted in Overworld blocks (a Nether block is 8). Engine on: only the starting value of `difficulty.json → factors.distance.per`. |
| `time.days_per_level` | number | `4` | 0.01 and up | **Days per level.** Engine off: one level per this many in-game days. Engine on: only the starting value of `factors.days.per`. |
| `time.max_levels` | number | `15` | 0 and up | **Most levels from days.** Engine off: cap on levels from days. Engine on: only the starting value of `factors.days.max`. |
| `depth.start_y` | integer | `32` | any | **Depth starts at Y.** Depth levels count blocks below this height (dimensions with a sky only). Engine on: only the starting value of `factors.depth.start_y`. |
| `depth.blocks_per_level` | number | `12` | 1 and up | **Depth blocks per level.** Engine off: one level per this many blocks below `start_y`. Engine on: only the starting value of `factors.depth.per`. |
| `depth.max_levels` | number | `8` | 0 and up | **Most levels from depth.** Engine off: cap on levels from depth. Engine on: only the starting value of `factors.depth.max`. |
| `spread` | integer | `2` | 0 and up | **Level spread.** Each mob's level is the threat plus a random whole number from -spread to +spread, kept inside the band. 0 makes every mob in a spot the same level. |
| `structures.<structure>` | integer | ancient city `40`, end city `50`, bastion `30`, mansion `30`, stronghold `25`, monument `25`, fortress `20`, trial chambers `20`, pillager outpost `12` | any (the band still applies) | **Structure minimum level.** Mobs spawning inside this structure (any structure id, modded too) get at least this threat. Applied before the player pull, so nearby low-level players can still pull it down. |
| `players.source` | string | `"ras"` | `"ras"`, `"vanilla"`, `"none"` | **Player level source.** What counts as a player's level: RPG Attribute System level (without RAS installed, players are ignored), vanilla experience level, or nothing. Also the level nameplate colours compare against. |
| `players.mode` | string | `"average"` | `"average"`, `"nearest"`, `"highest"` | **Player aggregation.** Which nearby player(s) count, for the level pull and for the engine's `played` and `gear` factors. Anything else counts as `average`. |
| `players.radius` | number | `64` | 1 and up | **Player radius.** Players within this many blocks of the spawn point count (also for `played` and `gear`). |
| `players.weight` | number | `0.35` | 0 to 1 | **Player pull.** How far the threat moves toward the nearby players' level: 0 ignores players, 1 makes mobs match them, 0.35 moves it about a third of the way. Works up and down. |
| `attributes[].attribute` | string | 7 rules, see below | attribute id | **Attribute.** The attribute a rule raises. `max_health`, `minecraft:max_health` and `minecraft:generic.max_health` all work; modded attributes too. Unknown ids are skipped. |
| `attributes[].mode` | string | see below | `"additive"`, `"percent"`, `"multiplicative"` | **Mode.** `additive`: +`per_level` per level. `percent`: +`per_level`% of the mob's base value per level. `multiplicative`: compounds `per_level`% per level. A missing or unknown mode counts as `percent`. |
| `attributes[].per_level` | number | see below | any | **Per level.** How much one level adds (a value for `additive`, a percentage otherwise). Negative values lower the attribute. |
| `attributes[].max` | number | see below | any; 0 = no cap | **Cap.** The most the rule can add (a value for `additive`, a percentage otherwise). The sign is ignored. |
| `scale_projectile_damage` | boolean | `true` | | **Scale ranged damage.** Arrows, tridents, fireballs and other damage that does not come from the mob's own attack damage scale like attack damage (level, rank and Berserk). |
| `mobs.exclude` | list of strings | `[]` | entity ids, `#` tags, `namespace:` | **Excluded mobs.** Mobs that never get a level (and so no rank or traits). The `#remnant_mobs:never_leveled` tag does the same. |
| `mobs.level_locks` | object | `{}` | entity id → level (0 and up) | **Level locks.** Fixed levels per mob type, e.g. `{"minecraft:warden": 40}`. Ignores threat, spread and band; the rank still rolls. |
| `mobs.boss_stats` | boolean | `false` | | **Scale bosses.** Bosses (`#remnant_mobs:bosses`: Ender Dragon, Wither, Warden, Elder Guardian, `#c:bosses`, Remnant Bosses) show a level on their plate; set true to also scale their stats and experience. |
| `hunters_moon.enabled` | boolean | `true` | | **Hunter's Moon.** Whether a Hunter's Moon can rise. Switching it off also ends one that is up. |
| `hunters_moon.chance` | number | `0.35` | 0 to 1 | **Hunter's Moon chance.** Chance, rolled once per full-moon night, that a Hunter's Moon rises. It lasts until dawn, in dimensions with a day cycle. |
| `hunters_moon.level_bonus` | integer | `5` | 0 and up | **Hunter's Moon levels.** Levels added to the threat while it is up (also the "+N" on the meter). |
| `hunters_moon.rank_chance_multiplier` | number | `2.0` | 0 and up | **Hunter's Moon rank odds.** Rank chances of mobs spawning under it are multiplied by this. |
| `hunters_moon.shard_multiplier` | number | `2.0` | 0 and up | **Hunter's Moon shards.** Remnant Shards dropped by mobs that spawned under it (or die under it) are multiplied by this. |

Default `attributes` rules (the cap is reached at level 100, toughness at 120):

| Attribute | Mode | `per_level` | `max` | At level 30 |
| --- | --- | --- | --- | --- |
| `minecraft:max_health` | `percent` | 6 | 600 | +180% |
| `minecraft:attack_damage` | `percent` | 3 | 300 | +90% |
| `minecraft:armor` | `additive` | 0.15 | 15 | +4.5 |
| `minecraft:armor_toughness` | `additive` | 0.05 | 6 | +1.5 |
| `minecraft:movement_speed` | `percent` | 0.3 | 30 | +9% |
| `minecraft:knockback_resistance` | `additive` | 0.005 | 0.5 | +0.15 |
| `minecraft:follow_range` | `percent` | 0.5 | 50 | +15% |

How the modifiers stack: `additive` rules are added to the base, `percent` rules multiply the base, `multiplicative`
rules and ranks multiply the total. A mob's max health is `base x (1 + level %) x rank health x (1 + Juggernaut)`.
Bosses keep their stats unless `mobs.boss_stats` is on; tamed mobs are never leveled.

### `ranks.json`

How often each rank appears and what it gives a mob. Read by the server (the Hunter's Lens also reads the client's copy
for its reward line).

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` | | **Ranks.** When false, every leveled mob is Common: no ranks, no traits. |
| `level_chance_divisor` | number | `50` | 1 and up | **Level rank bonus.** Rank chances are multiplied by `1 + level / this`: x1.6 at level 30 by default. Lower means ranks get common faster with level. |
| `spawner_multiplier` | number | `0.25` | 0 and up | **Spawner rank odds.** Rank chances of mobs from monster spawners are multiplied by this. |
| `ranks.<rank>.chance` | number | Veteran `0.06`, Elite `0.015`, Champion `0.003`, Remnant `0.0003` | 0 to 1 | **Base chance.** Chance before multipliers. Ranks are checked from Remnant down to Veteran and the first that rolls wins. |
| `ranks.<rank>.min_level` | integer | Veteran `1`, Elite `8`, Champion `15`, Remnant `30` | 0 and up | **Minimum level.** The rank never rolls on a mob below this level. |
| `ranks.<rank>.health` | number | Veteran `1.4`, Elite `2.0`, Champion `3.2`, Remnant `5.0` | 0.1 and up | **Health multiplier.** Multiplies the leveled max health. |
| `ranks.<rank>.damage` | number | Veteran `1.1`, Elite `1.25`, Champion `1.45`, Remnant `1.7` | 0.1 and up | **Damage multiplier.** Multiplies the leveled attack damage (and ranged damage with `scale_projectile_damage`). |
| `ranks.<rank>.armor` | number | Veteran `2`, Elite `4`, Champion `6`, Remnant `10` | any | **Extra armor.** Armor points added. |
| `ranks.<rank>.traits` | integer | Veteran `1`, Elite `2`, Champion `3`, Remnant `4` | 0 to 4 | **Traits.** How many traits it rolls (fewer if not enough traits are allowed). |
| `ranks.<rank>.experience` | number | Veteran `2`, Elite `3`, Champion `5`, Remnant `10` | 0 and up | **Experience multiplier.** Multiplies dropped experience (after the level bonus in `rewards.json`). |
| `ranks.<rank>.loot_rolls` | integer | Veteran `0`, Elite `1`, Champion `2`, Remnant `3` | 0 and up | **Extra loot rolls.** Rolls of the mob's loot table on top of the normal drop (`rewards.json → loot.extra_rolls`). |
| `ranks.<rank>.shards_min` | integer | Veteran `1`, Elite `2`, Champion `4`, Remnant `8` | 0 and up | **Fewest shards.** Fewest Remnant Shards it drops (`rewards.json → loot.shards`). |
| `ranks.<rank>.shards_max` | integer | Veteran `1`, Elite `3`, Champion `6`, Remnant `12` | at least `shards_min` | **Most shards.** Most Remnant Shards it drops. |
| `remnant.titles` | boolean | `true` | | **Remnant titles.** Remnant-rank mobs get a generated title ("Vorgrim the Hollow") unless they already have a name. |
| `remnant.boss_bar` | boolean | `true` | | **Remnant boss bar.** A boss bar for players within 32 blocks who can see the Remnant. |
| `remnant.announce` | boolean | `true` | | **Remnant kill message.** A server-wide message when a Remnant falls, naming who slew it. |
| `remnant.horn_radius` | number | `48` | 0 and up; 0 = off | **Remnant horn radius.** Players within this many blocks hear a horn and see a message when a Remnant spawns. |

The full rank chance: `chance x (1 + level / level_chance_divisor) x difficulty rank odds x event night x Hunter's Moon x
spawner`. With nothing else on, a level 30 mob rolls Remnant at 0.048%, Champion at 0.48%, Elite at 2.4% and Veteran at
9.6%, each checked only if the rarer ones missed.

### `traits.json`

Which traits ranked mobs can roll, how often, and their numbers. Read by the server. Every trait has the four keys in
the first table, plus its own numbers. Infernal and Frostbite never roll together, nor Swift and Juggernaut.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `<trait>.enabled` | boolean | `true` | | **Enabled.** Whether the trait can roll. |
| `<trait>.weight` | integer | `10` (Withering, Crushing, Juggernaut, Warded, Blinking `8`; Packleader, Volatile `6`) | 0 and up; 0 = never | **Weight.** Relative roll chance against the other allowed traits. |
| `<trait>.min_rank` | string | `"veteran"` (Withering, Juggernaut, Warded, Blinking, Packleader `"elite"`) | `veteran`, `elite`, `champion`, `remnant` | **Minimum rank.** The lowest rank that can roll it. `common` or an unknown value counts as `veteran`. |
| `<trait>.exclude` | list of strings | `[]` | entity ids, `#` tags, `namespace:` | **Excluded mobs.** Mobs that never get this trait. |

The traits' own numbers. No range is enforced: keep fractions between 0 and 1 and times, counts and powers at 0 or
above.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `vampiric.heal_fraction` | number | `0.3` | 0 to 1 | **Vampiric: heal.** Heals this fraction of the damage it deals. |
| `frostbite.slowness_seconds` | number | `3` | 0 and up | **Frostbite: slow time.** Seconds of Slowness on a hit. |
| `frostbite.slowness_level` | integer | `2` | 1 and up | **Frostbite: slow level.** Slowness level (2 = Slowness II). |
| `frostbite.freeze_ticks` | integer | `100` | 0 and up | **Frostbite: frost.** Ticks of powder-snow frost added per hit (the target is frozen from 140). Frostbite mobs never freeze. |
| `infernal.fire_seconds` | number | `4` | 0 and up | **Infernal: burn time.** Seconds a hit sets the target on fire. Infernal mobs are immune to fire. |
| `venomous.poison_seconds` | number | `4` | 0 and up | **Venomous: poison time.** Seconds of Poison on a hit (Poison I, Poison II from Champion). |
| `withering.wither_seconds` | number | `3` | 0 and up | **Withering: wither time.** Seconds of Wither I on a hit. |
| `berserk.below_health` | number | `0.5` | 0 to 1 | **Berserk: threshold.** Goes berserk below this fraction of its health. |
| `berserk.damage_bonus` | number | `0.4` | 0 and up | **Berserk: damage.** Extra damage while berserk (0.4 = +40%). |
| `berserk.speed_bonus` | number | `0.25` | 0 and up | **Berserk: speed.** Extra speed while berserk. |
| `crushing.knockback` | number | `1.5` | 0 and up | **Crushing: knockback.** Extra knockback strength of its melee hits. |
| `crushing.shield_seconds` | number | `3` | 0 and up | **Crushing: shield break.** Seconds a blocked melee hit disables the shield. |
| `juggernaut.health_bonus` | number | `0.5` | 0 and up | **Juggernaut: health.** Extra max health (0.5 = +50%). Juggernauts also ignore knockback. |
| `juggernaut.armor` | number | `4` | any | **Juggernaut: armor.** Armor points added. |
| `juggernaut.speed_penalty` | number | `0.1` | 0 to 1 | **Juggernaut: slowness.** Movement speed taken away (0.1 = 10% slower). |
| `thornskin.reflect` | number | `0.25` | 0 and up | **Thornskin: reflect.** Fraction of melee damage it takes that hurts the attacker. |
| `warded.shield_fraction` | number | `0.25` | 0 and up | **Warded: shield.** Absorption worth this fraction of its max health. |
| `warded.regrow_seconds` | number | `6` | 0 and up | **Warded: regrow time.** The shield regrows this many seconds after it was last hurt. |
| `regenerating.per_second` | number | `0.02` | 0 to 1 | **Regenerating: rate.** Fraction of max health healed per second out of combat. |
| `regenerating.in_combat` | number | `0.005` | 0 to 1 | **Regenerating: combat rate.** Fraction healed per second while in combat. |
| `regenerating.combat_seconds` | number | `3` | 0 and up | **Regenerating: combat time.** It counts as in combat for this long after it was hurt. |
| `deflecting.reduction` | number | `0.6` | 0 to 1 | **Deflecting: reduction.** Projectiles deal this much less damage to it (0.6 = 60% less). |
| `swift.speed_bonus` | number | `0.3` | 0 and up | **Swift: speed.** Extra movement speed (0.3 = +30%). |
| `blinking.chance` | number | `0.3` | 0 to 1 | **Blinking: chance.** Chance, when hit, to teleport behind its attacker. |
| `blinking.cooldown_seconds` | number | `5` | 0 and up | **Blinking: cooldown.** Seconds between teleports. |
| `blinking.range` | number | `8` | 0 and up | **Blinking: range.** It only teleports when the attacker is within twice this many blocks (16 by default). |
| `packleader.health_threshold` | number | `0.6` | 0 to 1 | **Packleader: threshold.** Calls its pack once, when its health drops to this fraction. |
| `packleader.minions` | integer | `2` | 0 and up | **Packleader: pack size.** How many copies it calls. Minions are Common and drop no extra loot, shards or Valor. |
| `packleader.minion_levels` | integer | `5` | 0 and up | **Packleader: minion levels.** Minions are this many levels lower (at least level 1). |
| `volatile.power` | number | `2.5` | 0 and up | **Volatile: power.** Explosion power after death (a creeper is 3). It never breaks blocks, and spares the mob's own drops. |
| `volatile.fuse_seconds` | number | `1.5` | 0 and up | **Volatile: fuse.** Seconds between death and the blast. |

### `rewards.json`

Experience, loot, Remnant Shards and RPG Attribute System Valor. Read by the server.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `experience.per_level` | number | `0.04` | 0 and up | **Experience per level.** Dropped experience is multiplied by `1 + level x this`, then by the rank's `experience`. Bosses keep vanilla experience unless `mobs.boss_stats` is on. |
| `loot.extra_rolls` | boolean | `true` | | **Extra loot.** Ranked mobs roll their loot table again (`ranks.json → loot_rolls`; `loot.json` adds more). |
| `loot.shards` | boolean | `true` | | **Remnant Shards.** Ranked mobs drop Remnant Shards. |
| `loot.require_player_kill` | boolean | `true` | | **Player kill only.** Extra loot and shards only when a player took part in the kill (keeps mob farms in check). Vital Embers, healing items and loot bags always need a player kill. |
| `valor.enabled` | boolean | `true` | | **Bonus Valor.** With RPG Attribute System: bonus Valor for the player who kills a ranked mob, on top of RAS's own kill Valor. Does nothing without RAS. |
| `valor.per_rank.<rank>` | number | Common `0`, Veteran `10`, Elite `25`, Champion `60`, Remnant `150` | 0 and up | **Valor per rank.** Base bonus per rank. Only ranked mobs pay, so `common` has no effect. |
| `valor.per_level` | number | `1.0` | 0 and up | **Valor per level.** Added per mob level: bonus = `(per_rank + level x per_level) x challenge`. |
| `valor.challenge.max` | number | `1.5` | 1 and up | **Challenge bonus.** The bonus multiplier for a mob well above your RAS level. |
| `valor.challenge.max_at` | integer | `10` | 1 and up | **Challenge bonus at.** Levels above you where `max` is reached (linear up to it). |
| `valor.challenge.min` | number | `0.25` | 0 to 1 | **Weak-mob penalty.** The multiplier for a mob well below your RAS level. |
| `valor.challenge.min_at` | integer | `15` | 1 and up | **Weak-mob penalty at.** Levels below you where `min` is reached. |
| `valor.message` | boolean | `true` | | **Valor message.** Show the bonus above the hotbar. |
| `shards.valor` | number | `15` | 0 and up | **Shard Valor.** Valor per Remnant Shard absorbed, with RAS. |
| `shards.experience` | integer | `8` | 0 and up | **Shard experience.** Experience points per Remnant Shard absorbed, without RAS. |
| `branded_multiplier` | number | `2.0` | 1 and up | **Brand multiplier.** Mobs marked with a Brand of Challenge multiply experience, extra loot, shards and Valor by this. |

### `difficulty.json`

The difficulty engine (3.1). When on, it replaces the distance, time and depth parts of `levels.json` with curved
factors, adds play time and gear, applies per-dimension multipliers and raises rank odds with difficulty. Bands,
structures, the player pull and spread stay in `levels.json`. Read by the server.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | fresh `true` / upgrade `false` | | **Difficulty engine.** Master switch. When false, levels follow `levels.json` exactly as in 3.0. |
| `factors.<factor>.enabled` | boolean | `true` for all five | | **Factor on.** When false the factor adds nothing. Factors: `days`, `played`, `distance`, `depth`, `gear`. |
| `factors.<factor>.curve` | string | see below | `"linear"`, `"sqrt"`, `"log"`, `"power"` | **Curve.** With `t = input / per`: `linear` is t, `sqrt` is √t (fast start, slow later), `log` is ln(1 + t) (slower still), `power` is t^exponent (slow start, fast later with exponent above 1). |
| `factors.<factor>.per` | number | see below | above 0 | **Step.** The input that makes t = 1: days, hours, blocks or gear points per level on a linear curve. |
| `factors.<factor>.exponent` | number | `1.0` | 0.01 and up | **Exponent.** Only used by `power`. |
| `factors.<factor>.max` | number | see below | any; 0 or less = no cap | **Cap.** Most levels the factor can add. |
| `factors.depth.start_y` | integer | `32` (from `levels.json → depth.start_y`) | any | **Depth start.** Depth counts blocks below this height, in dimensions with a sky. |
| `gear_score.armor` | number | `1.0` | 0 and up | **Gear: armor weight.** Gear score points per armor point. |
| `gear_score.toughness` | number | `1.5` | 0 and up | **Gear: toughness weight.** Points per armor toughness point. |
| `gear_score.attack_damage` | number | `1.0` | 0 and up | **Gear: attack weight.** Points per attack damage above the bare-hand 1. |
| `gear_score.extra_health` | number | `0.5` | 0 and up | **Gear: health weight.** Points per max health above 20 (Vital Embers and modded health count). |
| `rank_odds.max_multiplier` | number | `2.0` | 1 and up | **Rank odds at the top.** Rank chances are multiplied by up to this as the threat rises from the bottom (x1) to the top (x this) of the band. |
| `rank_odds.curve` | string | `"sqrt"` | `"linear"`, `"sqrt"`, `"log"`, `"power"` | **Rank odds curve.** How the multiplier rises across the band. `sqrt` gives x1.5 at 25% on the meter and x1.71 at 50%. `log` never reaches the full multiplier (x1.69 at 100% with the default 2); `power` acts like `linear` here. |
| `dimensions.<dimension>.multiplier` | number | Nether `1.15`, End `1.25` | 0 and up | **Dimension multiplier.** `(band min + factors) x multiplier`. It multiplies the band minimum too: the Nether starts at 17 instead of 15, the End at 43 instead of 35. Unlisted dimensions use 1. |
| `dimensions.<dimension>.offset` | number | `0` | any | **Dimension offset.** Levels added after the multiplier (negative to subtract). |
| `dimensions.<dimension>.factors` | object | `{}` | factor → any keys of a factor | **Factor overrides.** Overrides keys of a factor in this dimension only, e.g. `{"distance": {"per": 64}}`; the rest comes from `factors`. (`start_y` cannot be overridden per dimension.) |
| `exclude_dimensions` | list of strings | `["#remnant_mobs:no_difficulty"]` | dimension ids, `#` dimension type tags | **Excluded dimensions.** These keep the `levels.json` formula. The shipped tag is empty. |
| `compat.disable_overlaps` | boolean | fresh `true` / upgrade `false` | | **Avoid elite-mod overlap.** When a mod in `overlap_mod_ids` is installed, Remnants rolls no traits so the two do not stack. Levels, ranks and events stay. |
| `compat.overlap_mod_ids` | list of strings | `["champions", "infernalmobs"]` | mod ids | **Overlapping mods.** Mod ids that give monsters elite affixes. |

Default factors (written when the file is created; `days`, `distance` and `depth` copy the `levels.json` numbers of that
moment, so later edits to `levels.json` do not reach them):

| Factor | Input | `curve` | `per` | `exponent` | `max` | Example |
| --- | --- | --- | --- | --- | --- | --- |
| `days` | in-game days of the world | `linear` | 4 | 1 | 15 | 5 levels on day 20, 15 from day 60 |
| `played` | hours the nearby player(s) have played (vanilla statistic) | `sqrt` | 2 | 1 | 10 | 1 level at 2 h, 2 at 8 h, 5 at 50 h, 10 from 200 h |
| `distance` | blocks from world spawn, times the band's `distance_scale` | `linear` | 128 | 1 | 0 (no cap) | 7.8 levels at 1,000 blocks |
| `depth` | blocks below `start_y` | `linear` | 12 | 1 | 8 | 8 levels at Y -64 |
| `gear` | gear score of the nearby player(s) | `linear` | 6 | 1 | 15 | about 3 levels in full iron with an iron sword, 6 in diamond |

`played` and `gear` use `levels.json → players.mode` and `players.radius`; with nobody near, they add 0. Gear score is
`armor x armor + toughness x toughness + (attack damage - 1) x attack_damage + (max health - 20) x extra_health`, read
from the player's live attributes, so modded gear counts.

Mobs tagged `#remnant_mobs:ignores_difficulty` always use the `levels.json` formula.

### `events.json`

Event nights (3.1). Each entry under `events` is one kind of night; copy `grim_vigil` under a new id to add another.
The Hunter's Moon stays in `levels.json`. Read by the server.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | fresh `true` / upgrade `false` | | **Event nights.** Master switch for the scheduled events (not the Hunter's Moon). One event runs at a time and never during a Hunter's Moon. `/remnants event start <id>` runs one anyway. |
| `events.<event>.enabled` | boolean | `true` | | **Enabled.** Whether this night can roll. |
| `events.<event>.trigger.every_days` | integer | `10` | 1 and up | **Every N days.** It may roll every this many nights, counting from `first_day`. |
| `events.<event>.trigger.first_day` | integer | `7` | 0 and up | **First day.** The first in-game day it may roll on. |
| `events.<event>.trigger.chance` | number | `0.5` | 0 to 1 | **Chance.** Chance it starts on an eligible night (rolled at dusk). |
| `events.<event>.trigger.min_difficulty` | number | `0.0` | 0 to 1 | **Minimum difficulty.** Only when at least one player stands at this fraction of their band or more (0.25 = 25% on the meter). |
| `events.<event>.trigger.moon` | string | `"any"` | `"any"`, `"full"`, `"new"` | **Moon phase.** Only on full-moon or new-moon nights. Anything else counts as `any`. |
| `events.<event>.announce.title` | boolean | `true` | | **Title card.** A title on screen at dusk. |
| `events.<event>.announce.chat` | boolean | `true` | | **Chat line.** A chat message at dusk. |
| `events.<event>.announce.sound` | boolean | `true` | | **Horn.** A horn at dusk. |
| `events.<event>.effects.level_bonus` | integer | `3` | 0 and up | **Levels.** Levels added to the threat while it runs (in its dimensions; the "+N" on the meter). |
| `events.<event>.effects.rank_multiplier` | number | `1.5` | 0 and up | **Rank odds.** Rank chances of mobs spawning in its dimensions while it runs are multiplied by this. |
| `events.<event>.effects.shard_multiplier` | number | `1.5` | 0 and up | **Shards.** Remnant Shards dropped by its hunters and mini-boss are multiplied by this. |
| `events.<event>.hunters.waves` | integer | `3` | 0 to 20 | **Waves.** Waves of hunters per night. |
| `events.<event>.hunters.per_wave` | integer | `3` | 0 to 16 | **Hunters per wave.** Hunters sent after each player per wave. |
| `events.<event>.hunters.wave_seconds` | integer | `90` | 5 and up | **Wave interval.** Seconds between waves. |
| `events.<event>.hunters.first_wave_seconds` | integer | `20` | 0 and up | **First wave after.** Seconds after dusk before the first wave. Waves wait while nobody is online to hunt. |
| `events.<event>.hunters.min_rank` | string | `"veteran"` | `common`, `veteran`, `elite`, `champion`, `remnant` | **Hunter rank.** Hunters roll a rank as usual but are at least this rank. |
| `events.<event>.hunters.named` | boolean | `true` | | **Named hunters.** Hunters get generated titles. |
| `events.<event>.hunters.distance_min` | integer | `20` | 8 and up | **Spawn distance (min).** Nearest blocks from the player a hunter spawns. |
| `events.<event>.hunters.distance_max` | integer | `32` | at least `distance_min` | **Spawn distance (max).** Farthest blocks from the player. |
| `events.<event>.hunters.max_alive` | integer | `6` | 1 and up | **Hunters alive.** Most hunters alive per player; a wave sends fewer when this is reached. |
| `events.<event>.mini_boss.enabled` | boolean | `true` | | **Mini-boss.** A mini-boss (`#remnant_mobs:event_mini_bosses`) joins the last wave. |
| `events.<event>.mini_boss.per_player` | boolean | `false` | | **Mini-boss per player.** One per hunted player instead of one for a random player. |
| `events.<event>.mini_boss.rank` | string | `"remnant"` | `common`, `veteran`, `elite`, `champion`, `remnant` | **Mini-boss rank.** At least this rank. |
| `events.<event>.mini_boss.level_bonus` | integer | `5` | 0 and up | **Mini-boss levels.** Levels above the threat where it spawns (on top of `effects.level_bonus`), within the band. |
| `events.<event>.mini_boss.tier` | string | `"mini"` | `"mini"` or a tier another mod registers | **Boss tier.** Which boss-tier spawner makes it. Unknown tiers use `mini`. |
| `events.<event>.rewards.survive_experience` | integer | `50` | 0 and up | **Dawn experience.** Experience points for every hunted player alive at dawn. |
| `events.<event>.rewards.survive_valor` | number | `40` | 0 and up | **Dawn Valor.** Valor for the same players, with RPG Attribute System. |
| `events.<event>.rewards.boss_shards` | integer | `8` | 0 and up | **Mini-boss shards.** Extra Remnant Shards the mini-boss drops. |
| `events.<event>.dimensions` | list of strings | `["minecraft:overworld"]` | dimension ids, `#` dimension type tags | **Dimensions.** Where it runs: players elsewhere are not hunted and get no bonus levels. An empty list means nowhere. |

Hunters come from `#remnant_mobs:event_hunters`, spawn on valid spots inside the world border and are re-aimed at
their player every second. Players in creative or spectator mode are not hunted.

### `progression.json`

The player's side (3.1): Vital Embers (more hearts), healing items, where they drop, and the difficulty meter. Read by
the server, except the four meter keys marked "client", which each player's game reads from its own copy.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | fresh `true` / upgrade `false` | | **Progression.** Master switch: when false, ranked monsters drop none of the items below and the meter is not sent. Items players already have keep working. |
| `hearts.max_bonus_hearts` | integer | `10` | 0 to 100 | **Most extra hearts.** Vital Embers stop working at this many extra hearts. |
| `hearts.hearts_per_ember` | integer | `1` | 1 to 10 | **Hearts per Ember.** Hearts of max health one Vital Ember adds. |
| `hearts.keep_on_death` | boolean | `true` | | **Keep hearts on death.** The extra hearts survive death. |
| `drops.vital_ember.<rank>` | number | Common `0`, Veteran `0`, Elite `0.02`, Champion `0.15`, Remnant `0.6` | 0 to 1 | **Vital Ember chance.** Chance per player kill of a ranked monster (Common monsters never drop it). |
| `drops.vital_ember.event_boss` | number | `1.0` | 0 to 1 | **Vital Ember (mini-boss).** Chance for an event night's mini-boss. |
| `drops.mending_salve` | number | `0.06` | 0 to 1 | **Mending Salve chance.** Chance per player kill of a Veteran or higher. |
| `drops.hunters_draught` | number | `0.08` | 0 to 1 | **Hunter's Draught chance.** Chance per player kill of an Elite or higher. |
| `drops.difficulty_bonus` | number | `1.0` | 0 and up | **Difficulty drop bonus.** Every chance above is multiplied by `1 + this x difficulty` (difficulty 0 to 1: the mob's level across its band), so 1 doubles them at the top of the band. |
| `healing.mending_salve.heal` | number | `6` | 0 and up | **Salve: heal.** Health healed (6 = 3 hearts). |
| `healing.mending_salve.seconds` | integer | `3` | 1 and up | **Salve: duration.** Seconds the healing takes. |
| `healing.mending_salve.cooldown_seconds` | integer | `10` | 0 and up | **Salve: cooldown.** Seconds before the next one can be used. |
| `healing.hunters_draught.heal_fraction` | number | `0.4` | 0 to 1 | **Draught: heal.** Fraction of max health healed at once. |
| `healing.hunters_draught.regeneration_seconds` | integer | `5` | 0 and up | **Draught: regeneration.** Seconds of Regeneration I after drinking. |
| `healing.hunters_draught.cooldown_seconds` | integer | `30` | 0 and up | **Draught: cooldown.** Seconds before the next one can be drunk. |
| `meter.send_to_players` | boolean | `true` | | **Send the meter (server).** The server sends each player their difficulty once a second. False hides the meter for everyone. |
| `meter.hud` | boolean | `true` | | **Show the meter (client).** Draw the difficulty meter on your screen. False hides it for you only. |
| `meter.anchor` | string | `"bottom_right"` | `"top_left"`, `"top_right"`, `"bottom_left"`, `"bottom_right"` | **Meter corner (client).** The screen corner it sits in. |
| `meter.x` | integer | `6` | any | **Meter X (client).** GUI pixels from that corner's side. |
| `meter.y` | integer | `26` | any | **Meter Y (client).** GUI pixels from that corner's top or bottom (26 keeps it above the Hunter's Moon icon). |

### `elites.json`

Ranked monsters you can read in one second (3.1). Everything here is vanilla particles, boss bars, names and the glow
effect, so players without the mod see it too. Read by the server.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | fresh `true` / upgrade `false` | | **Readable elites.** Master switch. When false, Remnant boss bars stay yellow and nothing below applies. |
| `aura.enabled` | boolean | `true` | | **Rank aura.** Particles in the rank's colour around ranked monsters; Champions and Remnants get a turning ring. |
| `aura.min_rank` | string | `"elite"` | `veteran`, `elite`, `champion`, `remnant` | **Aura from rank.** Lowest rank with an aura. `common` or an unknown value counts as `elite`. |
| `aura.distance` | number | `24` | 4 to 64 | **Aura distance.** Players within this many blocks see it. |
| `boss_bars.champion` | boolean | `true` | | **Champion boss bars.** Champions get a boss bar like Remnants. |
| `boss_bars.remnant_color` | string | `"yellow"` | `pink`, `blue`, `red`, `green`, `yellow`, `purple`, `white` | **Remnant bar colour.** Colour of a Remnant's boss bar. |
| `boss_bars.champion_color` | string | `"purple"` | the same colours | **Champion bar colour.** Colour of a Champion's boss bar. |
| `boss_bars.event_boss_color` | string | `"red"` | the same colours | **Mini-boss bar colour.** For an event night's mini-boss. |
| `champion_titles` | boolean | `false` | | **Champion titles.** Champions get a generated title too, not only Remnants. |
| `event_glow` | boolean | `true` | | **Event glow.** Monsters sent by an event night glow (seen through walls) until dawn. |

### `loot.json`

Loot that grows with difficulty, bonus Mob LootBags bags and loot beams (3.1). "Difficulty" here is where the **mob's**
level sits in its dimension's band, 0 to 1. Read by the server.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | fresh `true` / upgrade `false` | | **Loot scaling.** Master switch for everything below. |
| `scaling.extra_rolls_bonus` | number | `1.0` | 0 and up | **Extra rolls bonus.** Ranked monsters roll their loot table `loot_rolls x (1 + this x difficulty)` extra times; a fraction is the chance of one more roll. 1 doubles the rolls at the top of the band. |
| `scaling.shards_bonus` | number | `0.5` | 0 and up | **Shards bonus.** The same for Remnant Shards (+50% at the top). |
| `scaling.common_extra_roll` | number | `0.15` | 0 to 1 | **Unranked extra roll.** Chance, times difficulty, that an unranked monster rolls its loot once more. |
| `loot_bags.enabled` | boolean | `true` | | **Mob LootBags bags.** With Mob LootBags installed, ranked monsters killed by a player may drop a bag on top of that mod's own drops. |
| `loot_bags.mod_id` | string | `"mob_loot_bags"` | mod id | **Mob LootBags mod id.** The mod id that tells Remnants Mob LootBags is installed. |
| `loot_bags.chance.<rank>` | number | Veteran `0.0`, Elite `0.1`, Champion `0.3`, Remnant `1.0`, `event_boss` `1.0` | 0 to 1 | **Bag chance.** Chance per rank; `event_boss` is an event night's mini-boss. |
| `loot_bags.tier.<rank>` | string | Veteran `"common"`, Elite `"uncommon"`, Champion `"rare"`, Remnant `"epic"`, `event_boss` `"legendary"` | `common`, `uncommon`, `rare`, `epic`, `legendary`, `mythic` | **Bag tier.** An unknown tier uses the default. |
| `loot_bags.tier_up_at_difficulty` | number | `0.66` | 0 to 1; above 1 = never | **Tier up at.** From this difficulty the bag is one tier better (never past mythic). |
| `loot_bags.items` | list of strings | the six bag ids, `mob_loot_bags:commonlootbag` to `mob_loot_bags:mythiclootbag` | item ids, lowest tier first | **Bag items.** Dropped by id when Mob LootBags' own bag code cannot be reached. |
| `beams.enabled` | boolean | `true` | | **Loot beams.** A column of light over rare items a ranked monster drops. |
| `beams.min_rarity` | string | `"rare"` | `common`, `uncommon`, `rare`, `epic` | **Beam rarity.** Lowest item rarity with a beam. An unknown value counts as `rare`. |
| `beams.seconds` | integer | `30` | 1 to 300 | **Beam time.** Seconds a beam lasts (it ends early when the item is picked up). |
| `beams.distance` | number | `48` | 8 to 128 | **Beam distance.** Players within this many blocks see it. |

### `client.json`

What you see. Read only by your own game, so every player can set it their own way.

| Key | Type | Default | Range / values | Description |
| --- | --- | --- | --- | --- |
| `nameplates.enabled` | boolean | `true` | | **Nameplates.** Plates above leveled mobs. |
| `nameplates.distance` | number | `24` | 2 to 64 | **Plate distance.** How far away ranked mobs and bosses show their plate (also any mob you look at). |
| `nameplates.common_distance` | number | `12` | 0 to 64; 0 = hide | **Common plate distance.** The same for Common mobs. |
| `nameplates.traits_distance` | number | `12` | 0 to 64 | **Traits distance.** How close you must be to read traits (always shown on the mob you look at). |
| `nameplates.look_at_only` | boolean | `false` | | **Only the mob you look at.** Show only the plate of the mob under your crosshair. |
| `nameplates.health_numbers` | boolean | `true` | | **Health numbers.** Current health beside the bar. |
| `nameplates.scale` | number | `1.0` | 0.5 to 2 | **Plate size.** Scale of the plates (1 is normal). |
| `nameplates.level_colors` | string | `"relative"` | `"relative"`, `"absolute"` | **Level colours.** `relative` colours a level against your own (RPG Attribute System, or experience levels if the server uses them); `absolute` keeps it neutral. |
| `nameplates.replace_name_tag` | boolean | `true` | | **Replace name tags.** Named mobs show their name on the plate instead of a separate name tag. |
| `nameplates.hide_with_hud` | boolean | `true` | | **Hide with the HUD.** Hide plates when the HUD is hidden (F1), like vanilla name tags. |
| `hud.lens` | boolean | `true` | | **Hunter's Lens HUD.** The Lens threat readout and its scope stat sheet. |
| `hud.hunters_moon` | boolean | `true` | | **Hunter's Moon icon.** A moon icon in the bottom right while a Hunter's Moon is up. |

## Version differences

The ten files have the **same keys, defaults and ranges in every workspace** (1.20.1 Fabric/Forge, 1.21.1
Fabric/NeoForge, 26.1.2, 26.2 and 26.3 Fabric/NeoForge/Forge): the config classes are the same code, and
`ConfigDocsTest` checks this page against the files each workspace writes. What differs is how the game under them
applies a few values:

| | 1.20.1 | 1.21.1 | 26.1.2, 26.2, 26.3 |
| --- | --- | --- | --- |
| In-game days (`days` factor, `time`, events, Hunter's Moon) | the Overworld's time of day | the same | the Overworld clock |
| Attribute ids in `attributes[].attribute` | the game names them `generic.max_health`; both spellings work | `max_health`; both work | the same |
| `infernal.fire_seconds` | rounded up to whole seconds | as written | as written |
| `crushing.shield_seconds` | puts the shield on cooldown | the same | disables the shield (the game's own shield-disable, for that long) |
| `frostbite.*` | Slowness and frost | the same | the same; from 26.4, the game's Freezing effect for `slowness_seconds` at `slowness_level` instead |
| Difficulty meter (`meter.*`) | drawn with the HUD | the same | the same; on Forge 26.x as its own HUD layer |
