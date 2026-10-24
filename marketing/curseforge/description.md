![Remnants: RPG Mobs 3.0: Blood and Valor](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/banner.png)

## Every monster has a level. Some have a rank. A few have a name.

**Remnants: RPG Mobs** turns Minecraft's monsters into RPG enemies. The farther you travel, the deeper you dig and the longer your world lives, the stronger they get. Some rise as **Veterans**, **Elites** and **Champions** with traits like *Vampiric*, *Blinking* or *Packleader*, and once in a few thousand spawns a **Remnant** walks out of the dark with a name, a title and a boss bar. Every kill pays more the harder it was: experience, extra loot, **Remnant Shards**, and **Valor** when you play with [RPG Attribute System](https://www.curseforge.com/minecraft/mc-mods/rpg-attribute-system).

**3.0 "Blood and Valor"** is a full rebuild of RPG Mob Leveling System under a new name, part of the Remnants family next to Remnant Bosses. Your 2.x configs and leveled mobs carry over.

![divider](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/divider.png)

![Levels](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-levels.png)

![Levels that mean something](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-levels.png)

A monster's level is the **threat** of the place it spawned:

- **Distance:** one level for every 128 blocks from world spawn. Distance counts in Overworld blocks, so the Nether is no shortcut.
- **Time:** one level every 4 in-game days, up to 15.
- **Depth:** one level every 12 blocks below Y 32, up to 8.
- **Ruins:** structures set a minimum. Ancient cities 40, end cities 50, bastions and mansions 30, strongholds and monuments 25, fortresses and trial chambers 20, pillager outposts 12.
- **Players:** with RPG Attribute System, nearby players pull mob levels a third of the way toward their own RAS level.

| Dimension | Levels |
| --- | --- |
| Overworld | 1 – 60 |
| The Nether | 15 – 80 |
| The End | 35 – 100 |
| Eclipsed Realm (Remnant Bosses) | 30 – 100 |
| Any other dimension | 1 – 60, configurable |

Every level adds +6% health, +3% damage, +0.15 armor, +0.3% speed and a little knockback resistance, each with a cap. A level 30 zombie has 56 health and hits for 5.7. Arrows, tridents and fireballs scale too. Tamed and passive mobs are left alone, and bosses show a level without being scaled. Type `/remnants threat` to see the breakdown where you stand.

![Ranks](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-ranks.png)

![From Common to Remnant](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-ranks.png)

Every hostile mob rolls a rank as it spawns. The chances grow with its level and double under a Hunter's Moon.

| Rank | Chance | From level | Health | Damage | Armor | Traits |
| --- | --- | --- | --- | --- | --- | --- |
| Common | | | ×1 | ×1 | | 0 |
| **Veteran** | 6% | 1 | ×1.4 | ×1.1 | +2 | 1 |
| **Elite** | 1.5% | 8 | ×2 | ×1.25 | +4 | 2 |
| **Champion** | 0.3% | 15 | ×3.2 | ×1.45 | +6 | 3 |
| **Remnant** | 0.03% | 30 | ×5 | ×1.7 | +10 | 4 |

![Remnants](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-remnant.png)

**Remnants** are the rarest and deadliest of all:

- a generated name and title, like *Vorgrim the Hollow*, and they never despawn;
- a horn heard by everyone within 48 blocks when one appears;
- a boss bar that follows it while it is in sight;
- a server-wide message when it falls, naming who slew it.

Mobs from monster spawners roll ranks at a quarter of the rate, so a spawner room stays a spawner room.

![Traits](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-traits.png)

![No two fights the same](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-traits.png)

Ranked mobs roll one to four of sixteen traits. Infernal and Frostbite never roll together, nor Swift and Juggernaut.

| Trait | From | Effect |
| --- | --- | --- |
| **Vampiric** | Veteran | Heals 30% of the damage it deals |
| **Frostbite** | Veteran | Hits slow (Slowness II) and freeze you. It cannot be frozen |
| **Infernal** | Veteran | Immune to fire. Hits set you on fire |
| **Venomous** | Veteran | Hits poison you, with Poison II from Champions up |
| **Withering** | Elite | Hits wither you |
| **Berserk** | Veteran | Below half health: +40% damage and +25% speed |
| **Crushing** | Veteran | Hits knock you far back and disable your shield |
| **Juggernaut** | Elite | Immune to knockback, +50% health and +4 armor, but 10% slower |
| **Thornskin** | Veteran | Reflects 25% of the melee damage it takes |
| **Warded** | Elite | A shield worth 25% of its health that regrows 6 seconds after the last hit |
| **Regenerating** | Veteran | Regains 2% of its health every second, less while fighting |
| **Deflecting** | Veteran | Takes 60% less damage from projectiles |
| **Swift** | Veteran | +30% movement speed |
| **Blinking** | Elite | When hit, may teleport behind its attacker |
| **Packleader** | Elite | At 60% health, calls two copies of itself five levels lower |
| **Volatile** | Veteran | Explodes 1.5 seconds after it dies. Step away. It never breaks blocks |

The Warded shield shatters and regrows with a sound you will learn to dread, Blinking mobs vanish and reappear behind you, Packleaders howl for help and Volatile corpses hiss before they blow.

![Nameplates](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-plates.png)

![Nameplates worth reading](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-plates.png)

- **Rank crest and name** in the rank's colour, and the **level** coloured against yours: grey far below, green below, white even, yellow and orange above, and **red with a skull** for anything 15 levels above you.
- **Health bar** in the rank colour (classic red for Common mobs), with a pale trail that drains away as you land hits, and the current health beside it.
- **Traits** appear when you come within 12 blocks or look at the mob.
- Common mobs show their plate within 12 blocks and ranked mobs within 24. Named mobs show their name on the plate. Plates hide with the HUD (F1).
- Distances, scale, look-at-only mode and colours are client settings.

![Rewards](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-rewards.png)

![Worth the risk](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-rewards.png)

- **Experience** grows 4% per level and is multiplied by the rank: ×2, ×3, ×5 and ×10. A level 30 Elite drops 6.6 times the usual.
- **Extra loot:** Elites roll their loot table once more, Champions twice and Remnants three times.
- **Remnant Shards** drop from every ranked mob: 1 from Veterans up to 8 – 12 from Remnants. Use one to absorb it, or sneak to absorb the stack.
- Extra loot and shards need a player in the kill, so mob farms do not get richer.

![RPG Attribute System](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-ras.png)

![Better with RAS](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-ras.png)

Remnants: RPG Mobs and [RPG Attribute System](https://www.curseforge.com/minecraft/mc-mods/rpg-attribute-system) are built for each other. RAS makes you stronger; Remnants gives that strength something to fight. Install both and:

- mob levels follow the RAS levels of the players nearby;
- every nameplate compares the mob's level with **your** RAS level;
- every ranked kill pays **bonus Valor**: Veteran 10, Elite 25, Champion 60, Remnant 150, plus one per level, on top of RAS's own kill Valor;
- that bonus rewards **punching up**: ×1.5 for a mob 10 levels above you, falling to ×0.25 for one 15 levels below;
- **Remnant Shards** absorb into 15 Valor each.

RAS is optional. Without it, levels follow distance, days and depth, and shards give experience.

![Items](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-items.png)

![Know thy enemy](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-lens.png)

![Raise the stakes](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-brand.png)

| Item | Recipe | Use |
| --- | --- | --- |
| **Remnant Shard** | Dropped by ranked mobs | Absorb for 15 Valor with RAS, or 8 experience points without it. Sneak to absorb the stack |
| **Hunter's Lens** | A spyglass with a Remnant Shard on each side | Held: the threat of the area, the levels of mobs there and how they compare with yours. Look through it at a mob for its full stat sheet: rank, level, traits, health, damage, armor and rewards |
| **Brand of Challenge** | An echo shard surrounded by eight Remnant Shards | Use it on a leveled mob: it rises one rank and five levels, gains at least one trait, heals, glows and comes for you. Every reward it drops is doubled |

![The Hunter's Moon](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-moon.png)

![The Hunter's Moon](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-moon.png)

Some full moons (35%) rise as a **Hunter's Moon**. A toll sounds and a title warns every player. Until dawn:

- mobs spawn **five levels higher**;
- ranks are **twice as likely**;
- shards drop **twice as often**.

A moon icon in the corner of the screen shows it is up. `/remnants moon` tells you how long until dawn.

![Configuration](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-config.png)

![Every number is yours](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/card-config.png)

Five commented JSON files in `config/remnant_mobs/`. Every key is explained in the file, missing keys come back with their defaults, and `/remnants reload` applies your changes without a restart.

- `levels.json`: dimension bands, distance, days, depth, structures, player scaling (`ras`, `vanilla` or `none`), attribute rules (any attribute, modded ones too), excluded mobs, level locks, boss scaling and the Hunter's Moon.
- `ranks.json`: rank chances, minimum levels, multipliers, trait counts and rewards; Remnant titles, boss bars, announcements and horn.
- `traits.json`: every trait's numbers, weight, minimum rank and excluded mobs.
- `rewards.json`: experience, extra loot, shards, Valor and the challenge curve.
- `client.json`: nameplate distances, scale, colours and HUD options.

Mob lists take entity ids (`minecraft:creeper`), tags (`#minecraft:skeletons`) or whole mods (`cobblemon:`).

| Command | Who | |
| --- | --- | --- |
| `/remnants threat` | Everyone | The threat breakdown where you stand |
| `/remnants inspect` | Everyone | Level, rank, traits and stats of the mob you look at |
| `/remnants moon` | Everyone | Is a Hunter's Moon up, and for how long |
| `/remnants spawn <entity> <level> [rank] [traits]` | Operators | Spawn a mob exactly as you want it |
| `/remnants set <targets> level / rank / traits / reroll / clear` | Operators | Change mobs that already exist |
| `/remnants moon start / stop` | Operators | Call or end a Hunter's Moon |
| `/remnants reload` | Operators | Reload the config files |

`/rml` works as a short alias.

![divider](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/divider.png)

![Everything Else](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-everything.png)

- **Ten advancements** on their own tab, from *Know Thy Enemy* to *A Remnant's End*.
- **Remnant Bosses:** its bosses keep their own fights (they show a level but are never ranked or scaled), and the Eclipsed Realm has its own level band.
- **Datapacks:** rank and trait scoreboard tags (`@e[tag=remnant_mobs.remnant]`, `remnant_mobs.trait.vampiric`) and the entity type tags `#remnant_mobs:bosses`, `#remnant_mobs:never_leveled` and `#remnant_mobs:never_ranked`.
- **For mod authors:** `RemnantMobsApi` reads levels, ranks and traits and sets levels. Every attribute modifier has a fixed id.
- New sounds for Remnants, the Hunter's Moon, shards, brands and traits, and animated textures for the Remnant Shard and the Brand of Challenge.

![Coming from 2.x](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-upgrading.png)

- The mod id is now `remnant_mobs`. Remove the old RPG Mob Leveling System jar; the two cannot be installed together.
- Your settings in `config/rpgmoblevelingsystem/` are carried over on first start: dimension levels, distance factor, player scaling, attribute rules, banned mobs and namespaces, level locks and structure minimums.
- Mobs leveled by 2.x keep their level. Ascendant elites become Elites.
- The Calendar item and the Neat and Jade hooks are gone. Levels show on the new nameplates.

![Downloads & Compatibility](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-compatibility.png)

| Minecraft | Loaders | Java | Also install |
| --- | --- | --- | --- |
| 1.20.1 | Fabric, Forge | 17+ | Fabric API (Fabric) |
| 1.21.1 | Fabric, NeoForge | 21+ | Fabric API (Fabric) |
| 26.1.2 | Fabric, NeoForge | 25+ | Fabric API (Fabric) |
| 26.2 | Fabric, NeoForge | 25+ | Fabric API (Fabric) |
| 26.3 | Fabric, NeoForge | 25+ | Fabric API (Fabric) |

- Download the file with your loader in its name: `-fabric-`, `-forge-` or `-neoforge-`.
- Install it on **both the server and every client**. The server does the leveling, the client draws the plates.
- [RPG Attribute System](https://www.curseforge.com/minecraft/mc-mods/rpg-attribute-system) is optional and recommended.
- Remnants: RPG Mobs is a mod. It does not work as a Paper, Spigot or Folia plugin.

![Questions](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-faq.png)

**Will this make the early game too hard?**
No. Near spawn on day one, mobs are level 1 to 3, and below level 8 the only rank that can appear is Veteran, with a single trait. The world gets more dangerous as you explore and as time passes, and with RAS it grows with you.

**Do I need RPG Attribute System?**
No, but it is the intended pairing. Without it, levels follow distance, days and depth, plates use neutral colours (or your experience level, if the server sets `players.source` to `vanilla`), and shards give experience.

**Does it work with modded mobs?**
Yes. Any hostile mob gets levels, ranks and traits. Keep a mob or a whole mod out with `mobs.exclude` in `levels.json`, or with the `#remnant_mobs:never_leveled` and `#remnant_mobs:never_ranked` tags. Modded bosses in `#c:bosses` are recognised as bosses.

**Can I make it harder or easier?**
Everything is a number in the config: the level bands, how fast levels grow, what a level does, how often each rank appears and what every trait does.

**I see a plate and a name tag, or no plate at all.**
Remnants must be installed on the client to draw plates. Named mobs show their name on the plate; set `replace_name_tag` to `false` in `client.json` to keep vanilla name tags as well.

**Does it slow my server down?**
A mob is leveled once, the first time it ticks, and its data is saved as entity tags. Trait effects only run on the mobs that have them.

**Where are the docs?**
[Usage guide](https://github.com/MeherBenSalem/Ascendant-Mobs/blob/main/docs/USAGE.md) · [API](https://github.com/MeherBenSalem/Ascendant-Mobs/blob/main/docs/API.md) · [Release notes](https://github.com/MeherBenSalem/Ascendant-Mobs/blob/main/patchnotes/3.0.0.md) · [Issues](https://github.com/MeherBenSalem/Ascendant-Mobs/issues)

![Gallery](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/header-gallery.png)

![Common to Remnant at sunset, against a level 25 hunter](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/ranks.jpg)

![A Remnant and its boss bar](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/remnant.jpg)

![A Champion's plate a moment after a heavy hit](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/plate.jpg)

![Holding the Hunter's Lens: the threat here and the levels monsters spawn at](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/lens-hud.jpg)

![Through the Hunter's Lens: a Champion's stat sheet](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/lens-scope.jpg)

![Bonus Valor for an Elite, with RPG Attribute System](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/valor.jpg)

![A Brand of Challenge turns an Elite into a Champion](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/brand.jpg)

![A Hunter's Moon rises](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/hunters-moon.jpg)

![/remnants threat and /remnants inspect](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/commands.jpg)

![Level 43 to 46 on a Nether fortress bridge](https://raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/screenshots/nether.jpg)

---

Remnants: RPG Mobs is licensed under Apache-2.0. The Cinzel and EB Garamond fonts used in this artwork are licensed under the SIL Open Font License 1.1.
