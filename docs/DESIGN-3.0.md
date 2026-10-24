# Remnants: RPG Mobs 3.0 — design

The next major version of RPG Mob Leveling System, renamed **Remnants: RPG Mobs** to join the Remnants family
(Remnant Bosses) and built to pair with RPG Attribute System (RAS). Everything is rebuilt; the leveling formulas,
attribute rules and dimension bands of 2.x carry over through a config migration.

## Identity

| | |
| --- | --- |
| Name | Remnants: RPG Mobs |
| Mod id | `remnant_mobs` (`remnants` is used by Remnant Bosses' creatures and by another mod) |
| Version | 3.0.0 "Blood and Valor" |
| Package / group | `com.nightbeam.remnants.mobs` / `com.nightbeam.remnants` |
| Versions | 1.20.1 (Fabric, Forge), 1.21.1, 26.1.2, 26.2, 26.3 (Fabric, NeoForge) — the same set as RAS |

## 1. Levels: the threat of a place

A hostile mob gets a level once, when it spawns (or the first time it ticks without one, for mobs from older
worlds). The level is the **threat** of its spawn point:

```
threat = band.min
       + horizontal distance from world spawn (× dimension distance scale) / blocks_per_level
       + min(days / days_per_level, max_day_levels)
       + depth: levels below depth_start_y, one per blocks_per_depth_level, up to max_depth_levels
       + Hunter's Moon bonus
threat = max(threat, structure minimum)                   e.g. ancient city 40, bastion 30
level  = threat + (player level − threat) × player_weight  only with RAS and a player in range
level  = clamp(level ± spread, band.min, band.max)
```

- Player level is the RAS level of the nearest / highest / average player within `player_radius`.
  Without RAS the player term is skipped (or uses vanilla XP levels when `player_source` is `vanilla`).
- Dimension bands: Overworld 1–60, Nether 15–80 (distance ×8), End 35–100, Eclipsed Realm (Remnant Bosses) 30–100.
  Unknown dimensions use the default band.
- `/remnants threat` prints the breakdown for where you stand; the Hunter's Lens shows it on screen.

Level scales attributes through **attribute rules** (2.x feature, kept): per attribute `additive`, `percent` or
`multiplicative`, a per-level value and a cap. New defaults are gentler and percentage based (health +6%/level,
damage +3%/level, armor +0.15/level, speed +0.3%/level, knockback resistance +0.5%/level). Modifiers use fixed
ids, so stats are never applied twice and base stats are never corrupted.

## 2. Ranks

Each leveled hostile mob rolls a rank. Chances grow with level and double under a Hunter's Moon.

| Rank | Base chance | Min level | Health | Damage | Armor | Traits | XP | Extra loot rolls | Shards |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Common | — | — | ×1 | ×1 | +0 | 0 | ×1 | 0 | 0 |
| Veteran | 6% | 1 | ×1.4 | ×1.1 | +2 | 1 | ×2 | 0 | 1 |
| Elite | 1.5% | 8 | ×2 | ×1.25 | +4 | 2 | ×3 | 1 | 2–3 |
| Champion | 0.3% | 15 | ×3.2 | ×1.45 | +6 | 3 | ×5 | 2 | 4–6 |
| Remnant | 0.03% | 30 | ×5 | ×1.7 | +10 | 4 | ×10 | 3 | 8–12 |

- Chance multiplier: `1 + level / 50`.
- **Remnant** rank mobs get a generated title ("Vorgrim the Hollow"), a boss bar for nearby players, a horn
  when they appear near a player and a server announcement when slain.
- Bosses (`#remnant_mobs:bosses`: vanilla bosses, `#c:bosses`, Remnant Bosses) get a level but no rank or traits.
- Datapack selectors: rank tags `remnant_mobs.veteran` … `remnant_mobs.remnant`.

## 3. Traits

Ranked mobs roll traits (no duplicates, incompatible pairs excluded, per-trait mob exclusions).

| Trait | Effect |
| --- | --- |
| Vampiric | Heals 30% of the damage it deals |
| Frostbite | Hits slow (Slowness II, 3 s) and freeze; immune to freezing |
| Infernal | Fire immune; hits set you on fire |
| Venomous | Hits poison (Poison II from Champion) |
| Withering | Hits wither |
| Berserk | Below half health: +40% damage, +25% speed |
| Crushing | Heavy knockback; hits disable shields |
| Juggernaut | Immune to knockback, +50% health, +4 armor, 10% slower |
| Thornskin | Reflects 25% of melee damage |
| Warded | A shield of absorption (25% max health) that regrows after 6 s out of combat |
| Regenerating | Regains 2% health per second out of combat |
| Deflecting | Takes 60% less damage from projectiles |
| Swift | +30% movement speed |
| Blinking | When hit, may teleport behind its attacker (5 s cooldown) |
| Packleader | At 60% health, calls two lesser copies of itself, once |
| Volatile | Explodes 1.5 s after death (no block damage) |

Incompatible: Infernal/Frostbite, Swift/Juggernaut. All numbers live in `traits.json`.

## 4. Nameplates

A soulslike plate above leveled mobs, drawn with a bitmap font so it renders identically on every version:

```
  ◆ 42  Elite Zombie          rank crest, level (danger colour), name in rank colour
  ▕██████████░░░░░▏           health bar in the rank colour
  Vampiric · Swift            traits, when you look at it or stand close
```

- Level colour is relative to **your RAS level** when RAS is installed: grey (10+ below), green, white (±2),
  orange, red, and a skull at 15+ above. Without RAS it follows absolute level bands.
- `client.json`: show distance, traits distance, health numbers, look-at-only mode, plate scale, colour mode.

## 5. Rewards

- **Experience**: × (1 + level × 0.04) × rank multiplier.
- **Loot**: Elite+ roll the mob's loot table extra times.
- **Remnant Shards** drop from Veteran+ (more under a Hunter's Moon). Use one to absorb it — sneak to absorb a
  stack: grants **Valor** through RAS (`/ras xp`), or vanilla XP without RAS.
- **RAS bonus Valor** on every ranked kill: `rank bonus × challenge`, where challenge rewards fighting above your
  level (×1.5 at +10) and fades for mobs far below it (×0.25 at −15). RAS already awards Valor from the victim's
  max health, so higher levels pay more Valor automatically.

## 6. Items

| Item | Recipe | Use |
| --- | --- | --- |
| Remnant Shard | drop | Absorb for Valor (RAS) or XP |
| Hunter's Lens | spyglass + 4 shards | Held: shows the threat of the area. Use on a mob: full stat sheet (level, rank, traits, health, damage, armor, level breakdown) |
| Brand of Challenge | 8 shards + echo shard | Use on a leveled mob: +1 rank (up to Remnant), +5 levels, one extra trait, double rewards |

## 7. Hunter's Moon

On full-moon nights (chance per full moon, default 35%) a **Hunter's Moon** rises: a bell, a title and a chat
line at dusk; mobs spawn +5 levels with double rank chances and double shards until dawn. `/remnants moon
start|stop`. Config: `moon` section of `levels.json`.

## 8. RAS integration

RAS is optional. With it:
- mob levels follow nearby players' RAS levels (`player_weight`, default 0.35);
- nameplate colours compare the mob's level with yours;
- ranked kills and Remnant Shards grant Valor;
- RAS's own kill Valor already scales with the mob's (scaled) max health;
- the Hunter's Lens shows the recommended RAS level for an area.

Access is through `RasApi` (reflection, no compile dependency) and the public `/ras xp <amount> <player>` command.

## 9. Commands (`/remnants`, alias `/rml`)

| Command | Permission | |
| --- | --- | --- |
| `threat` | all | Threat breakdown at your position |
| `inspect [target]` | all (look) / ops (target) | Level, rank, traits, stats of a mob |
| `spawn <entity> <level> [rank] [traits…]` | ops | Spawn a leveled mob |
| `set <targets> level|rank|trait …` | ops | Change existing mobs |
| `moon start|stop|status` | ops / all | Hunter's Moon |
| `reload` | ops | Reload configs |

## 10. Config (`config/remnant_mobs/`)

Commented JSON, missing keys restored, `/remnants reload` applies them:

- `levels.json` — bands, distance/time/depth, structures, RAS player scaling, attribute rules, mob filters, moon
- `ranks.json` — rank chances, multipliers, rewards
- `traits.json` — per-trait toggles, weights, numbers, exclusions
- `rewards.json` — XP, loot, shards, Valor
- `client.json` — nameplates and HUD

2.x configs in `config/rpgmoblevelingsystem/` are migrated once (dimension bands, distance factor, attribute
rules, banned mobs and namespaces, level locks, structure minimums, player scaling mode).

## 11. Data, sync and storage

- Per mob: one entity tag `remnant_mobs:data|<level>|<rank>|<traits>|<seed>` plus rank tags. Attribute
  modifiers have fixed ids (`remnant_mobs:level/<attribute>`, `remnant_mobs:rank/<attribute>`, `remnant_mobs:trait/<id>`).
- 2.x mobs (`am_level_<n>` tags) keep their level and get a rank roll of Common.
- Clients receive `remnant_mobs:mob_info` when they start tracking a mob or its data changes, and
  `remnant_mobs:player_level` with their RAS level.

## 12. Advancements

Remnants (root) · Proven Hunter (Elite) · Champion's Bane (Champion) · Remnant's End (Remnant) · Punching Up
(+10 levels) · Under the Hunter's Moon · Taste of Valor (shard) · Know Thy Enemy (lens) · Accept the Challenge
(brand) · Nothing Left to Hide (four traits).
