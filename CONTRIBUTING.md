# Contributing

Thanks for helping improve Remnants: RPG Mobs.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Prefer small, focused changes with a clear purpose.
3. Match the existing code style and MultiLoader layout. A gameplay change belongs in every version workspace
   (`1.20.1/`, `1.21.1/`, `26.1.2/`, `26.2/`, `26.3/`); 26.3 is the reference the others are ported from.
4. Add or update tests when practical (`common` module JUnit).
5. Open a pull request describing what changed and why.

## Development

- Java 17 for `1.20.1` (Fabric + Forge), Java 21 for `1.21.1`, Java 25 for `26.1.2`, `26.2` and `26.3`
  (Fabric + NeoForge).
- Build a workspace with `./gradlew build` from its folder. Shared logic lives in each workspace's `common` module.
- Textures, sounds, models, language files, recipes, tags and advancements are generated: edit
  `tools/assets/` and run `python3 tools/assets/build.py all` instead of editing the files in `resources/`.
- `python3 tools/qa/run_all.py <version> <loader>` runs the in-game checks on a dev server and client.

## Licensing

By contributing, you agree that your contributions are licensed under the
Apache License, Version 2.0, unless you state otherwise in the pull request.
