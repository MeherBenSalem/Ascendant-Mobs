/**
 * Upload local dist/ jars to CurseForge (no GitHub Actions). NightBeam publishes to CurseForge only
 * (since 24 Nov 2026); the existing Modrinth project stays up as it is but gets no new versions.
 * Tokens: env vars or NightBeam-Knowledge-Base/secrets/local.env
 *
 * Usage:
 *   node scripts/upload_platforms.mjs --version 3.0.0
 *   node scripts/upload_platforms.mjs --version 3.0.0 --dry-run
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..");
const DIST = path.join(ROOT, "dist");

const CURSEFORGE_ID = "1088344";
const MOD_TITLE = "Remnants: RPG Mobs";

async function loadEnv() {
  const candidates = [
    path.join(ROOT, "secrets", "local.env"),
    path.join(process.env.USERPROFILE || "", "Desktop", "local.env"),
    path.join(process.env.USERPROFILE || "", "NightBeam-Knowledge-Base", "secrets", "local.env"),
    "C:\\Users\\Meher\\Desktop\\local.env",
    "C:\\Users\\mahou\\NightBeam-Knowledge-Base\\secrets\\local.env",
  ];
  for (const envPath of candidates) {
    if (!fs.existsSync(envPath)) continue;
    const text = fs.readFileSync(envPath, "utf8");
    for (const line of text.split(/\r?\n/)) {
      const m = line.match(/^([^#=]+)=(.*)$/);
      if (!m) continue;
      const k = m[1].trim();
      const v = m[2].trim();
      if (!process.env[k]) process.env[k] = v;
    }
    console.log("Loaded secrets from", envPath);
    return;
  }
}

function parseArgs(argv) {
  const out = { dryRun: false };
  for (let i = 2; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--version") out.version = argv[++i];
    else if (a === "--changelog-file") out.changelogFile = argv[++i];
    else if (a === "--dry-run") out.dryRun = true;
    else throw new Error(`Unknown arg ${a}`);
  }
  if (!out.version) throw new Error("--version required");
  return out;
}

function parseJar(jar) {
  const n = path.basename(jar);
  let loader = "fabric";
  if (/neoforge/i.test(n)) loader = "neoforge";
  else if (/forge/i.test(n)) loader = "forge";
  else if (/fabric/i.test(n)) loader = "fabric";
  const m = n.match(/^remnant_mobs-(?:fabric|forge|neoforge)-(.+)-(\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?)\.jar$/i);
  const game = m ? m[1] : null;
  return { jar, name: n, loader, game };
}

async function main() {
  await loadEnv();
  const args = parseArgs(process.argv);
  const version = args.version.replace(/^v/, "");

  const changelog = args.changelogFile && fs.existsSync(args.changelogFile)
    ? fs.readFileSync(args.changelogFile, "utf8")
    : `## ${MOD_TITLE} ${version}\n\nSee GitHub release notes.`;

  const jars = fs
    .readdirSync(DIST)
    .filter((f) => f.startsWith("remnant_mobs-") && f.endsWith(`-${version}.jar`))
    .map((f) => path.join(DIST, f))
    .sort();
  if (!jars.length) throw new Error(`No remnant_mobs-*-${version}.jar in dist/`);

  const parsed = jars.map(parseJar);
  for (const p of parsed) {
    if (!p.game) throw new Error(`Cannot parse MC version from ${p.name}`);
  }

  const { CURSEFORGE_TOKEN, CURSEFORGE_API_KEY } = process.env;
  if (!CURSEFORGE_TOKEN || !CURSEFORGE_API_KEY) {
    throw new Error("Set CURSEFORGE_TOKEN, CURSEFORGE_API_KEY (or secrets/local.env)");
  }

  console.log(`Uploading ${MOD_TITLE} ${version} (${jars.length} jars) → CurseForge ${CURSEFORGE_ID}`);

  const LOADER_IDS = { fabric: 7499, forge: 7498, neoforge: 10150 };
  const legacyRes = await fetch("https://minecraft.curseforge.com/api/game/versions", {
    headers: { "X-Api-Token": CURSEFORGE_TOKEN },
  });
  if (!legacyRes.ok) throw new Error(`CurseForge legacy versions API ${legacyRes.status}`);
  const legacyFlat = await legacyRes.json();
  const findLegacy = (want, preferType) => {
    const hits = legacyFlat.filter((v) => v.name === want);
    if (preferType != null) {
      const hit = hits.find((v) => v.gameVersionTypeID === preferType);
      if (!hit) throw new Error(`No CF legacy version for ${want} (type ${preferType})`);
      return hit.id;
    }
    if (!hits[0]) throw new Error(`No CF legacy version for ${want}`);
    return hits[0].id;
  };
  const clientId = findLegacy("Client");
  const serverId = findLegacy("Server");

  const resolveGameId = async (game) => {
    const verRes = await fetch(
      `https://api.curseforge.com/v1/minecraft/version/${encodeURIComponent(game)}`,
      { headers: { "x-api-key": CURSEFORGE_API_KEY, Accept: "application/json" } },
    );
    if (verRes.ok) {
      const verJson = await verRes.json();
      const gvId = verJson.data?.gameVersionId;
      if (gvId) {
        console.log("CF game", game, "-> gameVersionId", gvId);
        return gvId;
      }
    }
    const typesRes = await fetch("https://api.curseforge.com/v1/games/432/version-types", {
      headers: { "x-api-key": CURSEFORGE_API_KEY, Accept: "application/json" },
    });
    const typeIds = [];
    if (typesRes.ok) {
      const typesJson = await typesRes.json();
      for (const t of typesJson.data || []) {
        if (/minecraft/i.test(t.name) && !/bukkit|spigot|paper|plugin/i.test(t.name)) {
          typeIds.push(t.id);
        }
      }
    }
    typeIds.push(77784, 1);
    const slug = game.replace(/\./g, "-");
    const candidates = legacyFlat.filter((v) => v.name === game || v.slug === slug);
    for (const typeId of typeIds) {
      const hit = candidates.find((v) => v.gameVersionTypeID === typeId);
      if (hit) {
        console.log("CF game", game, "-> legacy id", hit.id, "type", hit.gameVersionTypeID);
        return hit.id;
      }
    }
    throw new Error(`No CF game version id for ${game}`);
  };

  for (const p of parsed) {
    const gameId = await resolveGameId(p.game);
    const loaderId = LOADER_IDS[p.loader];
    if (!loaderId) throw new Error(`Unknown loader for ${p.name}`);
    const meta = {
      changelog,
      changelogType: "markdown",
      displayName: p.name,
      gameVersions: [clientId, serverId, loaderId, gameId],
      releaseType: "release",
    };
    if (args.dryRun) {
      console.log("[dry-run] CurseForge", p.name, meta);
      continue;
    }
    const cfForm = new FormData();
    cfForm.append("metadata", JSON.stringify(meta));
    cfForm.append("file", new Blob([fs.readFileSync(p.jar)]), p.name);
    let cfText = "";
    for (let attempt = 1; attempt <= 4; attempt++) {
      const cfRes = await fetch(
        `https://minecraft.curseforge.com/api/projects/${CURSEFORGE_ID}/upload-file`,
        { method: "POST", headers: { "X-Api-Token": CURSEFORGE_TOKEN }, body: cfForm },
      );
      cfText = await cfRes.text();
      if (cfRes.ok) {
        console.log("CurseForge OK", p.name, cfText.slice(0, 120));
        break;
      }
      if ((cfRes.status === 503 || cfRes.status === 429) && attempt < 4) {
        console.warn("CurseForge", cfRes.status, "for", p.name, "- retry", attempt);
        await new Promise((r) => setTimeout(r, 15000 * attempt));
        continue;
      }
      throw new Error(`CurseForge ${cfRes.status} ${p.name} ${cfText.slice(0, 500)}`);
    }
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
