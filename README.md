# RPG Mob Leveling System

Minecraft mod that levels mobs as they spawn and scales their attributes, combat,
and rewards. Formerly known as Ascendant Mobs. Mod ID: `rpgmoblevelingsystem`.

## Features

- Distance / day / MOTP / RPG Attribute System based leveling
- Configurable attribute scaling (additive, percent, multiplicative)
- Optional Ascendant elites, structure/boss level locks, loot-by-level
- Client level display via legacy overlay, or soft integration with [Neat](https://modrinth.com/mod/neat) plates
- MultiLoader: Fabric + Forge (1.20.1), Fabric + NeoForge (1.21.1)

## Requirements

| Minecraft | Loaders | Java |
|-----------|---------|------|
| 1.20.1 | Fabric, Forge | 17 |
| 1.21.1 | Fabric, NeoForge | 21 |

## Installation

Install the jar that matches your Minecraft version and loader from
[Modrinth](https://modrinth.com/mod/rpg-mob-leveling-system) or
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/rpg-mob-leveling-system).

## Configuration

Settings live under `config/rpgmoblevelingsystem/`. See [docs/USAGE.md](docs/USAGE.md).

## Building

```bash
# 1.20.1 (Java 17)
cd 1.20.1 && ./gradlew build

# 1.21.1 (Java 21)
cd 1.21.1 && ./gradlew build
```

Local release helpers: `publish_local.ps1` (build + GitHub release) and
`upload_local.ps1` (Modrinth + CurseForge from `dist/`).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md).

## License

Licensed under the [Apache License 2.0](LICENSE).
