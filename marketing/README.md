# Marketing assets: 3.0 "Blood and Valor"

Listing copy and artwork for CurseForge and Modrinth. Nothing here ships in the mod JARs.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full project description (Markdown; also works on Modrinth) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short project summary (231 characters) |
| [`../patchnotes/3.0.0.md`](../patchnotes/3.0.0.md) | File changelog for 3.0.0 |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/Ascendant-Mobs/main/marketing/images/...`, so they work once this folder is on `main`. To host on CurseForge instead, upload the images to the project gallery and replace each URL.

The description leads with the one-line pitch, then levels, ranks and Remnants, traits, nameplates, rewards, RPG Attribute System, the two items, the Hunter's Moon, configuration and commands, everything else, upgrading from 2.x, downloads and compatibility, the FAQ and a gallery.

The project is still listed as RPG Mob Leveling System. Rename it to **Remnants: RPG Mobs** on both sites, then replace the description, summary, icon and gallery.

## Adding a Minecraft version

The supported versions are listed in exactly two places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '26.4', loaders: ['Fabric', 'NeoForge'] },`. The banner's loader pills and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row.

No other copy or artwork names a Minecraft version. A new release name or number goes in `RELEASE` in `render.cjs` and in the first lines of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600, 1286 KB | Description header: the five ranks in a cherry grove at sunset, "3.0 · Blood and Valor", loader pills and versions |
| `images/icon.png` | 512×512, 163 KB | Project avatar: the mod logo (horned skull before a blood moon) in a gold ring |
| `images/card-levels.png` | 1600×900, 1309 KB | The Hunter's Lens readout in the grove; Overworld, Nether and End bands |
| `images/card-ranks.png` | 1600×900, 1154 KB | The five ranks side by side, with chance, minimum level, health and traits |
| `images/card-remnant.png` | 1600×900, 1118 KB | A Remnant close up with its boss bar; ×5 health, ×1.7 damage, 4 traits, 8–12 shards |
| `images/card-traits.png` | 1600×900, 638 KB | All sixteen traits with their in-game icons and colours |
| `images/card-plates.png` | 1600×900, 1274 KB | A Champion's plate with its damage trail, and the six level colours |
| `images/card-rewards.png` | 1600×900, 1123 KB | Rewards per rank (experience, extra loot, shards, Valor) and a Valor kill |
| `images/card-ras.png` | 1600×900, 1062 KB | A Valor kill and the challenge curve (×0.25 to ×1.5) |
| `images/card-lens.png` | 1600×900, 1223 KB | The Hunter's Lens held and through the scope |
| `images/card-brand.png` | 1600×900, 1321 KB | A Brand of Challenge landing; +1 rank, +5 levels, +1 trait, ×2 rewards |
| `images/card-moon.png` | 1600×900, 979 KB | A Hunter's Moon rising; 35%, +5 levels, ×2 ranks, ×2 shards |
| `images/card-config.png` | 1600×900, 627 KB | Excerpts of `levels.json` and `traits.json` |
| `images/header-*.png` | 1400×120, transparent, 81–107 KB | 14 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/screenshots/*.jpg` | 1920×1080, 188–502 KB | 10 captures: gallery uploads and card art |

### How the screenshots were captured

Every screenshot is an unedited capture of the **26.3 Fabric development client** (`tools/qa/devrun.sh start-client 26.3 fabric`, Remnants: RPG Mobs 3.0.0 and RPG Attribute System 5.0.0, default configuration) with software rendering at 1920×1080 and GUI scale 3. [`source/capture.py`](source/capture.py) takes them all; run it with the dev server and client up to take them again.

- **World:** a fresh world, "showcase", seed `remnants-showcase`, on day 40 (a full-moon day). The grove shots are in a cherry grove 260 blocks from spawn at sunset; the Nether shot is on a fortress bridge.
- **Mobs** are spawned with `/remnants spawn` at chosen levels, ranks and traits, and frozen with `NoAI` so they hold still. Plates, titles, boss bars, the Lens readout, the stat sheet, Valor messages and the Brand all come from the mod as it runs.
- **The hunter** is RPG Attribute System level 25, so plate colours run from grey (level 12) to red with a skull (level 52).
- **Lighting:** the grove shots use Night Vision, which evens out the sunset light; the Nether shot uses the in-game Bright setting instead.
- **HUD:** `ranks`, `plate` and `nether` hide the HUD (F1) with `hide_with_hud` turned off in `client.json`, so only the plates show. The others keep it, with the RAS level bar at the top left.
- **Timing:** `plate.jpg` is the best frame of a short burst after a hit, while the damage trail still shows.

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots, the mod's own icon font and the bundled fonts:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- `source/backdrop.jpg` is the blurred background scene, a 1280×720 copy of `screenshots/ranks.jpg`.
- `source/logo.png` is the mod logo (`common/src/main/resources/logo.png`).
- `source/icons.png` is the mod's icon font (`assets/remnant_mobs/textures/font/icons.png`): rank crests, trait icons and the skull. The cards tint each 8×8 glyph the way Minecraft tints text.
- `source/fonts/` holds Cinzel and EB Garamond with their SIL Open Font License 1.1 texts. The script uses only these files.

Rank and trait colours, rank odds, rewards and trait numbers on the cards are written at the top of `render.cjs`, copied from `content/Rank.java`, `content/Trait.java` and the default config. Update them when those change. Card crops are pixel regions of the 1920×1080 captures (`region()` calls), so new captures may need their crops adjusted.

## Wording

Every number in the copy and on the cards is a 3.0.0 default from the generated config (`config/remnant_mobs/`), a recipe, or the code. Update the copy when those defaults change. Call the rarest rank **Remnant** and the currency **Valor** (RPG Attribute System's term).
