# Remnants: RPG Mobs

Every monster gets a level, some get a rank, and the rarest come back with traits, titles and a boss bar.
Remnants: RPG Mobs turns the mobs of Minecraft into RPG enemies: the farther, deeper and longer you go, the
stronger they get, and the better they pay. It pairs with
[RPG Attribute System](https://github.com/MeherBenSalem/rpg-attribute-system) (RAS): monsters measure
themselves against your RAS level and ranked kills pay out Valor.

Formerly **RPG Mob Leveling System** (Ascendant Mobs). Mod id: `remnant_mobs`. 3.0 is a full rebuild; 2.x
configs and leveled mobs carry over.

## Features

- **Levels**: the threat of a place grows with distance from spawn, world age, depth and dimension. Ruins such
  as ancient cities and bastions have their own minimums, and with RAS nearby players pull levels toward their
  own.
- **Ranks**: Veteran, Elite, Champion and Remnant mobs hit harder, take more punishment, and drop more
  experience, extra loot and Remnant Shards.
- **16 traits**: Vampiric, Frostbite, Infernal, Venomous, Withering, Berserk, Crushing, Juggernaut, Thornskin,
  Warded, Regenerating, Deflecting, Swift, Blinking, Packleader and Volatile.
- **Remnants**: the rarest rank gets a generated title ("Vorgrim the Hollow"), a boss bar, a horn when it
  appears and a server-wide message when it falls.
- **Nameplates**: rank crest, level coloured by danger relative to you, a health bar in the rank's colour with
  a trailing damage segment, and traits once you get close.
- **Hunter's Moon**: some full moons turn into a Hunter's Moon. Until dawn, mobs spawn stronger, ranks double and shards
  double.
- **Items**: the Hunter's Lens (threat readout, stat sheet of any mob through the scope) and the Brand of
  Challenge (raise a mob's rank for double rewards).
- **RAS integration**: levels follow RAS levels, plate colours compare with yours, and kills and shards grant
  Valor. Everything works without RAS too.
- 10 advancements, `/remnants` commands, commented JSON configs with `/remnants reload`, an in-game settings screen
  (Mod Menu, the Mods list or `/remnants config`) and a small API.

## Requirements

| Minecraft | Loaders | Java | Folder |
| --- | --- | --- | --- |
| 1.20.1 | Forge, Fabric | 17 bytecode (build on 21) | `1.20.1/{common,forge,fabric}` |
| 1.21.1 | NeoForge, Fabric | 21 | `1.21.1/{common,neoforge,fabric}` |
| 26.2 | NeoForge, Fabric | 25 | `26.2/{common,neoforge,fabric}` |
| 26.3 | NeoForge, Fabric | 25 | `26.3/{common,neoforge,fabric}` |

That is 8 jars per release, all with the same version. Other Minecraft versions and loaders (26.1.2, Forge on 26.x)
are no longer built or supported.

Fabric needs Fabric API. RPG Attribute System is optional. Install the mod on both the server and the client:
the server does the leveling and the client draws the plates.

## Documentation

- [docs/USAGE.md](docs/USAGE.md): how levels, ranks, traits and rewards work, every config file and command
- [docs/CONFIG.md](docs/CONFIG.md): every config option with its default, range and effect; how difficulty and the
  difficulty meter work; which keys control how strong mobs get; recipes (slower or faster scaling, level caps, hiding
  the meter, an easier early game)
- [docs/API.md](docs/API.md): `RemnantMobsApi`, entity tags and modifier ids for other mods and datapacks
- [PATCH_NOTES.md](PATCH_NOTES.md) and [patchnotes/3.0.0.md](patchnotes/3.0.0.md): what changed
- [marketing/](marketing/): CurseForge and Modrinth page, summary and images

## Building

```bash
./gradlew buildAll             # every workspace, then the 8 loader jars into releases/
cd 1.20.1 && ./gradlew build   # Gradle on Java 21 (Java 17 toolchain)
cd 1.21.1 && ./gradlew build   # Java 21
cd 26.3 && ./gradlew build     # Java 25 (the same for 26.2)
```

The root build hands the 26.x workspaces `JAVA_HOME_25_X64` (or `-Pjava25Home=<jdk>`) and the others
`JAVA_HOME_21_X64` (or `-Pjava21Home=<jdk>`); without them each workspace uses the current `JAVA_HOME`.
Jars land in `<workspace>/<loader>/build/libs/remnant_mobs-<loader>-<minecraft>-<version>.jar`. Every workspace's
`gradle.properties` carries the same `version=` (`./gradlew checkVersions`; set it with
`./gradlew syncVersion -PmodVersion=x.y.z`).

## Releasing

CI (`.github/workflows/ci.yml`) builds all 8 jars on every push to `main`. Publishing to Modrinth and CurseForge is
`.github/workflows/publish.yml`, run by hand (`version`, `dry_run`) or by a `v*.*.*` tag; it never runs on a plain
push. It refuses a second release of the mod on the same day (Africa/Tunis), and fails unless exactly the 8 jars
are found. It needs the secrets `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, `CURSEFORGE_API_KEY` and the variables
`MODRINTH_ID`, `CURSEFORGE_ID`.

## Tools

- `python3 tools/assets/build.py <version|all>` regenerates every texture, sound, model, language file, recipe,
  tag and advancement from code into each workspace.
- `python3 tools/qa/run_all.py <version> <loader>` starts a dev server and client and runs the in-game checks
  (`tools/qa/server_smoke.py`, `tools/qa/client_checks.py`).
- `python3 tools/port_setup.py` lays out a new version workspace from the closest existing one.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md).

## License

Licensed under the [Apache License 2.0](LICENSE).
