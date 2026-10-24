// Renders the listing artwork in marketing/images/ from real in-game screenshots, the mod's own icon font and the
// bundled OFL fonts. Usage (from the repository root): NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility table of curseforge/description.md).
const RELEASE = { version: '3.0', name: 'Blood and Valor' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Fabric', 'Forge'] },
  { minecraft: '1.21.1', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.1.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// Colours and numbers match the code and the default config (content/Rank.java, content/Trait.java,
// config/RanksConfig.java, config/TraitsConfig.java, client/ClientState.java).
const RANKS = [
  { key: 'common', name: 'Common', color: '#cfc8b8', icon: [0, 0] },
  { key: 'veteran', name: 'Veteran', color: '#9bc46a', icon: [0, 1], chance: '6%', from: 1, health: '×1.4', damage: '×1.1', traits: 1, xp: '×2', loot: '—', shards: '1', valor: 10 },
  { key: 'elite', name: 'Elite', color: '#5fa8e8', icon: [0, 2], chance: '1.5%', from: 8, health: '×2', damage: '×1.25', traits: 2, xp: '×3', loot: '+1', shards: '2–3', valor: 25 },
  { key: 'champion', name: 'Champion', color: '#b57beb', icon: [0, 3], chance: '0.3%', from: 15, health: '×3.2', damage: '×1.45', traits: 3, xp: '×5', loot: '+2', shards: '4–6', valor: 60 },
  { key: 'remnant', name: 'Remnant', color: '#f0a43a', icon: [0, 4], chance: '0.03%', from: 30, health: '×5', damage: '×1.7', traits: 4, xp: '×10', loot: '+3', shards: '8–12', valor: 150 },
];
const TRAITS = [
  ['Vampiric', '#d23246', 'Heals 30% of the damage it deals'],
  ['Frostbite', '#8fd8ff', 'Hits slow and freeze you'],
  ['Infernal', '#ff7a1a', 'Fire immune, hits set you ablaze'],
  ['Venomous', '#6ccb3a', 'Hits poison, harder from Champion'],
  ['Withering', '#8c8c9c', 'Hits wither you'],
  ['Berserk', '#e8492e', 'Below half health: +40% damage'],
  ['Crushing', '#c49a62', 'Heavy knockback, breaks shields'],
  ['Juggernaut', '#a6a6b8', 'No knockback, +50% health'],
  ['Thornskin', '#4faf48', 'Reflects 25% of melee damage'],
  ['Warded', '#f2d46b', 'A shield that regrows'],
  ['Regenerating', '#f58fb0', 'Heals 2% a second'],
  ['Deflecting', '#c8e8ff', '60% less projectile damage'],
  ['Swift', '#9fe8e0', '+30% movement speed'],
  ['Blinking', '#b060ff', 'Teleports behind you when hit'],
  ['Packleader', '#e0a85c', 'Calls two lesser copies'],
  ['Volatile', '#ff5a2e', 'Explodes after it dies'],
].map(([name, color, text], i) => ({ name, color, text, icon: [1, i] }));
// Level colours against your own level, from client/ClientState.levelColor.
const LEVEL_COLORS = [['−10', '#9a9a9a', 'far below'], ['−3', '#7fd86a', 'below'], ['±2', '#f2f2f2', 'even'],
  ['+3', '#f2d24b', 'above'], ['+7', '#f2903a', 'well above'], ['+15', '#e8403a', 'deadly']];

const css = `
@font-face { font-family: RbSerif; src: url('${url(path.join(fonts, 'eb-garamond.ttf'))}'); }
@font-face { font-family: RbDisplay; src: url('${url(path.join(fonts, 'cinzel.ttf'))}'); }
:root { --text:#dad3c1; --dim:#9c9584; --faint:#6e685c; --header:#e8dfc8; --gold:#d4b06a; --gold-hi:#f0d58c;
        --blood:#c8423a; --blood-hi:#ef6a52; --bg:#0b0908; --line:rgba(216,207,184,.32); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--bg); color: var(--text); font-family: RbSerif, serif; }
.stage { position: relative; overflow: hidden; background: radial-gradient(ellipse at 30% 40%, #1f1512 0%, #0b0908 70%); }
.display { font-family: RbDisplay, serif; letter-spacing: .04em; }
.gold { background: linear-gradient(180deg, #fff2c8 0%, #f0d58c 35%, #d4b06a 70%, #8c7444 100%);
        -webkit-background-clip: text; background-clip: text; color: transparent; }
.glow { filter: drop-shadow(0 0 18px rgba(212,176,106,.35)); }
.kicker { font-family: RbDisplay, serif; color: var(--gold); letter-spacing: .22em; font-size: 22px; }
.rule { height: 1px; background: linear-gradient(90deg, rgba(212,176,106,0), rgba(212,176,106,.9) 20%, rgba(212,176,106,.9) 70%, rgba(212,176,106,0)); }
.diamond { display: inline-block; width: 9px; height: 9px; background: var(--blood-hi); transform: rotate(45deg); margin: 0 12px 3px 0; box-shadow: 0 0 10px rgba(239,106,82,.7); }
.frame { position: relative; border: 1px solid rgba(216,207,184,.45); box-shadow: 0 0 0 1px rgba(0,0,0,.6), 0 30px 80px rgba(0,0,0,.65), 0 0 60px rgba(200,66,58,.16); background: #000; }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.vignette { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(0,0,0,0) 45%, rgba(0,0,0,.75) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(16px) brightness(.4) saturate(1.1); }
.pill { display: inline-block; border: 1px solid rgba(216,207,184,.55); padding: 6px 14px 4px; margin-right: 10px;
        font-family: RbDisplay, serif; font-size: 18px; letter-spacing: .12em; color: var(--header); background: rgba(0,0,0,.35); }
.label { position: absolute; font-family: RbDisplay, serif; font-size: 20px; letter-spacing: .14em; color: var(--header);
         background: rgba(10,9,8,.86); border: 1px solid rgba(212,176,106,.8); padding: 7px 14px 5px; white-space: nowrap;
         box-shadow: 0 8px 26px rgba(0,0,0,.6); z-index: 2; }
.label small { display: block; font-family: RbSerif, serif; letter-spacing: 0; font-size: 18px; color: var(--dim); margin-top: 1px; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 26px; margin: 0 0 14px; font-size: 27px; line-height: 1.32; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 2px; top: 13px; width: 8px; height: 8px; background: var(--blood-hi); transform: rotate(45deg); }
.stats { display: flex; margin-top: 22px; border: 1px solid rgba(216,207,184,.35); background: rgba(10,9,8,.82); box-shadow: 0 18px 50px rgba(0,0,0,.55); }
.stats > div { flex: 1; padding: 14px 24px 12px; border-left: 1px solid rgba(216,207,184,.2); }
.stats > div:first-child { border-left: 0; }
.stats .t { font-family: RbDisplay, serif; font-size: 16px; letter-spacing: .18em; color: var(--dim); }
.stats .v { font-family: RbDisplay, serif; font-size: 30px; color: var(--header); margin-top: 4px; white-space: nowrap; }
b.hi { color: var(--header); font-weight: normal; }
table.grid { border-collapse: collapse; width: 100%; background: rgba(10,9,8,.88); border: 1px solid rgba(216,207,184,.35); box-shadow: 0 18px 50px rgba(0,0,0,.55); }
table.grid th { font-family: RbDisplay, serif; font-weight: normal; font-size: 15px; letter-spacing: .16em; color: var(--dim); text-align: left; padding: 14px 14px 10px; border-bottom: 1px solid rgba(216,207,184,.25); }
table.grid td { font-family: RbDisplay, serif; font-size: 25px; color: var(--header); padding: 12px 14px 10px; border-bottom: 1px solid rgba(216,207,184,.12); white-space: nowrap; }
table.grid tr:last-child td { border-bottom: 0; }
canvas.icon { image-rendering: pixelated; vertical-align: middle; }
`;

// Slow embers, the blood-moon counterpart of the golden motes of the sibling kits. Deterministic so renders are reproducible.
const motes = `<canvas class="motes"></canvas><script>
(() => { const c = document.querySelector('canvas.motes'); const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = 11;
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  for (let i = 0; i < 90; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .25 + rnd() * .6, z = 1 + rnd() * 2.2;
    const warm = rnd() < .35 ? '239,106,82' : '246,214,150';
    const h = g.createRadialGradient(x, y, 0, x, y, z * 5); h.addColorStop(0, 'rgba(' + warm + ',' + a * .55 + ')'); h.addColorStop(1, 'rgba(' + warm + ',0)');
    g.fillStyle = h; g.beginPath(); g.arc(x, y, z * 5, 0, Math.PI * 2); g.fill();
    g.fillStyle = 'rgba(255,236,200,' + a + ')'; g.fillRect(x - z / 2, y - z / 2, z, z); } })();
</script>`;

// The mod's icon font (assets/remnant_mobs/textures/font/icons.png, 8x8 cells), tinted the way Minecraft tints text.
const iconScript = `<script>
(() => { const sheet = new Image(); sheet.src = '${source('icons.png')}';
  sheet.onload = () => { for (const c of document.querySelectorAll('canvas.icon')) {
    const [row, col] = c.dataset.i.split(',').map(Number); const size = +c.dataset.s; c.width = c.height = size;
    const g = c.getContext('2d'); g.imageSmoothingEnabled = false;
    g.drawImage(sheet, col * 8, row * 8, 8, 8, 0, 0, size, size);
    g.globalCompositeOperation = 'multiply'; g.fillStyle = c.dataset.c; g.fillRect(0, 0, size, size);
    g.globalCompositeOperation = 'destination-in'; g.drawImage(sheet, col * 8, row * 8, 8, 8, 0, 0, size, size); }
    document.body.dataset.icons = 'ready'; }; })();
</script>`;
const icon = ([row, col], color, size = 48, style = '') => `<canvas class="icon" data-i="${row},${col}" data-c="${color}" data-s="${size}" style="width:${size}px;height:${size}px;${style}"></canvas>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div>${iconScript}</body></html>`;

const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:28px;background:linear-gradient(90deg,rgba(0,0,0,0),rgba(22,16,14,.9) 30%,rgba(22,16,14,.9) 70%,rgba(0,0,0,0))">
    <div class="rule" style="width:300px;transform:scaleX(-1)"></div>
    <span class="diamond" style="margin:0"></span>
    <div class="display gold glow" style="font-size:44px;white-space:nowrap">${title}</div>
    <span class="diamond" style="margin:0"></span>
    <div class="rule" style="width:300px"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.55"></div>
  <div class="vignette"></div>${motes}
  <div style="position:absolute;left:80px;top:0;bottom:0;width:${width - artWidth - 150}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 24 ? ' style="font-size:18px;letter-spacing:.12em"' : ''}><span class="diamond"></span>${kicker}</div>
    <div class="display gold glow" style="font-size:58px;line-height:1.08;margin:18px 0 22px">${title}</div>
    <div class="rule" style="width:80%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:60px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in 1920x1080 pixels) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * 1920 / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '', color = '') => `<div class="frame" style="${color ? `border-color:${color}99;` : ''}${style}">${inner}</div>`;
const label = (text, style, small = '') => `<div class="label" style="${style}">${text}${small ? `<small>${small}</small>` : ''}</div>`;
const stats = (items, color) => `<div class="stats"${color ? ` style="border-top:2px solid ${color}"` : ''}>${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="t">${t}</div><div class="v">${v}</div></div>`).join('')}</div>`;
// Two overlapping captures.
const pair = (first, second, firstLabel, secondLabel, firstSpot = 'left:20px;top:20px', height = 700) => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:74%">${first}</div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:74%">${second}</div>
    ${label(firstLabel, firstSpot)}${label(secondLabel, 'right:20px;bottom:20px')}</div>`;
const img = (file) => `<img src="${shot(file)}" style="display:block;width:100%">`;

// The level colours of a nameplate, as a strip.
const levelStrip = () => `<div class="stats" style="margin-top:20px">${LEVEL_COLORS.map(([d, c, t], i) =>
  `<div style="text-align:center;padding:12px 8px 10px"><div class="v" style="color:${c};font-size:30px">${i === LEVEL_COLORS.length - 1 ? icon([2, 0], c, 26, 'margin:0 8px 6px 0') : ''}${d}</div>
   <div class="t" style="margin-top:2px;font-size:14px">${t}</div></div>`).join('')}</div>`;

// The Valor challenge curve of config/RewardsConfig.challenge: x0.25 at 15 levels below, x1 even, x1.5 at 10 above.
const challengeChart = () => {
  const W = 940, H = 300, pad = 56, x = (d) => pad + (d + 15) / 25 * (W - pad * 2), y = (m) => H - 44 - m / 1.6 * (H - 80);
  const curve = (d) => (d >= 0 ? 1 + 0.5 * Math.min(d, 10) / 10 : 1 - 0.75 * Math.min(-d, 15) / 15);
  const pts = []; for (let d = -15; d <= 10; d += .5) pts.push(`${x(d).toFixed(1)},${y(curve(d)).toFixed(1)}`);
  return `<div class="frame" style="background:rgba(12,10,9,.92);padding:22px 20px 8px">
    <div class="display" style="font-size:20px;letter-spacing:.16em;color:var(--dim);margin:0 0 4px 36px">BONUS VALOR × CHALLENGE</div>
    <svg width="${W}" height="${H}" style="display:block">
      ${[0.25, 1, 1.5].map((m) => `<line x1="${pad}" x2="${W - pad}" y1="${y(m)}" y2="${y(m)}" stroke="rgba(216,207,184,.18)"/>
        <text x="${pad - 10}" y="${y(m) + 7}" fill="#9c9584" font-family="RbDisplay" font-size="20" text-anchor="end">×${m}</text>`).join('')}
      ${[-15, -10, -5, 0, 5, 10].map((d) => `<text x="${x(d)}" y="${H - 12}" fill="#9c9584" font-family="RbDisplay" font-size="20" text-anchor="middle">${d > 0 ? '+' + d : d}</text>`).join('')}
      <line x1="${x(0)}" x2="${x(0)}" y1="20" y2="${H - 44}" stroke="rgba(212,176,106,.45)" stroke-dasharray="4 6"/>
      <polyline points="${pts.join(' ')}" fill="none" stroke="#ef6a52" stroke-width="5" stroke-linejoin="round"/>
      <circle cx="${x(10)}" cy="${y(1.5)}" r="8" fill="#f0d58c"/><circle cx="${x(-15)}" cy="${y(0.25)}" r="8" fill="#9a9a9a"/>
      <text x="${x(0) + 12}" y="40" fill="#d4b06a" font-family="RbDisplay" font-size="18">your level</text>
    </svg>
    <div style="display:flex;justify-content:space-between;font-size:22px;color:var(--dim);margin:2px 36px 10px">
      <span>farming far below you</span><span>levels above you</span></div></div>`;
};

const versionText = VERSIONS.map((v) => v.minecraft).join(' · ');

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('ranks.jpg')}" style="position:absolute;right:-20px;top:-120px;width:1450px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 38%);mask-image:linear-gradient(90deg,transparent 0%,#000 38%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(8,6,5,.96) 0%,rgba(8,6,5,.9) 30%,rgba(8,6,5,.35) 55%,rgba(8,6,5,0) 75%)"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(0deg,rgba(8,6,5,.7) 0%,rgba(8,6,5,0) 30%)"></div>
    <div class="vignette"></div>${motes}
    <div style="position:absolute;left:90px;top:80px;width:900px">
      <div class="kicker"><span class="diamond"></span>${RELEASE.version} · ${RELEASE.name}</div>
      <div class="display gold glow" style="font-size:84px;line-height:1.04;margin:22px 0 26px;white-space:nowrap">Remnants<br><span style="font-size:60px">RPG Mobs</span></div>
      <div class="rule" style="width:600px;margin-bottom:26px"></div>
      <div style="font-size:34px;color:var(--header);line-height:1.3">Every monster has a level. Some have a rank.<br>A few have a name.</div>
      <div style="margin-top:36px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}
        <span style="font-size:24px;color:var(--dim);margin-left:8px">${versionText}</span></div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 45%,#2e1c17 0%,#140d0b 55%,#070505 100%)"></div>${motes}
    <div style="position:absolute;left:78px;top:78px;width:356px;height:356px;border-radius:50%;overflow:hidden;box-shadow:inset 0 0 30px rgba(0,0,0,.8)">
      <img src="${source('logo.png')}" style="position:absolute;left:-14px;top:-14px;width:384px;height:384px;image-rendering:pixelated"></div>
    <div style="position:absolute;left:56px;top:56px;width:400px;height:400px;border-radius:50%;border:3px solid var(--gold);box-shadow:0 0 40px rgba(212,176,106,.45), inset 0 0 40px rgba(200,66,58,.3)"></div>
    <div style="position:absolute;left:78px;top:78px;width:356px;height:356px;border-radius:50%;border:1px solid rgba(212,176,106,.55)"></div>
    ${[[256, 56], [456, 256], [256, 456], [56, 256]].map(([x, y]) => `<div style="position:absolute;left:${x - 12}px;top:${y - 12}px;width:24px;height:24px;background:var(--gold);transform:rotate(45deg);box-shadow:0 0 16px rgba(240,213,140,.8)"></div>`).join('')}`),

  'card-levels': () => card({
    kicker: 'The threat of a place', title: 'Levels that<br>mean something',
    points: ['One level every 128 blocks from spawn', 'Stronger as the days pass, and deeper underground',
      'Ruins hold their own: ancient cities start at 40', 'With RAS, nearby players pull levels toward theirs',
      'Health, damage, armor and speed rise with every level'],
    art: framed(region('lens-hud.jpg', 240, 135, 1440, 810, 978)) +
      stats([['Overworld', '1 – 60'], ['Nether', '15 – 80'], ['The End', '35 – 100']]) }),

  'card-ranks': () => card({
    kicker: 'Five ranks', title: 'From Common<br>to Remnant',
    points: ['Every hostile mob rolls a rank as it spawns', 'Higher ranks unlock at higher levels',
      'More health, damage and armor, and one to four traits', 'Better pay: experience, extra loot and Remnant Shards',
      'Spawner mobs roll ranks at a quarter of the rate'],
    art: framed(region('ranks.jpg', 0, 300, 1920, 780, 978)) + `<table class="grid" style="margin-top:20px"><tr><th>RANK</th><th>CHANCE</th><th>FROM LV</th><th>HEALTH</th><th>TRAITS</th></tr>
      ${RANKS.slice(1).map((r) => `<tr><td style="color:${r.color}">${icon(r.icon, r.color, 26, 'margin:0 12px 5px 0')}${r.name}</td><td>${r.chance}</td><td>${r.from}</td><td>${r.health}</td><td>${r.traits}</td></tr>`).join('')}</table>` }),

  'card-remnant': () => card({
    kicker: 'One in a few thousand', title: 'Remnants',
    points: ['A name and a title of their own', 'A horn sounds when one appears near you', 'A boss bar follows it while it is in sight',
      'Four traits, five times the health', 'The whole server hears when it falls, and who slew it'],
    art: framed(region('remnant.jpg', 0, 0, 1920, 1080, 978), '', '#f0a43a') +
      stats([['Health', '×5'], ['Damage', '×1.7'], ['Traits', '4'], ['Shards', '8 – 12']], '#f0a43a') }),

  'card-traits': () => card({
    kicker: 'Sixteen traits', title: 'No two fights<br>the same',
    points: ['Ranked mobs roll one to four traits', 'Each with its own sounds and particles', 'Some only appear on Elites and above',
      'Every trait can be tuned, reweighted or turned off'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">${TRAITS.map((t) => `
      <div style="display:flex;align-items:center;gap:16px;background:rgba(12,10,9,.9);border:1px solid ${t.color}66;border-left:3px solid ${t.color};padding:10px 16px;box-shadow:0 10px 30px rgba(0,0,0,.45)">
        ${icon(t.icon, t.color, 40)}
        <div><div class="display" style="font-size:24px;color:${t.color}">${t.name}</div><div style="font-size:20px;color:var(--dim);margin-top:1px">${t.text}</div></div></div>`).join('')}</div>` }),

  'card-plates': () => card({
    kicker: 'Read the fight', title: 'Nameplates<br>worth reading',
    points: ['Rank crest and a name in the rank’s colour', 'The level coloured against your own level',
      'A health bar with a draining damage trail', 'Traits when you come close', 'Clean, readable and the same on every version'],
    art: framed(region('plate.jpg', 520, 420, 880, 495, 978)) + levelStrip() }),

  'card-lens': () => card({
    kicker: 'Hunter’s Lens', title: 'Know thy<br>enemy',
    points: ['Held: the threat of the area and the levels of mobs there', 'Shown against your own level',
      'Through the scope: a full stat sheet of any mob', 'Rank, traits, health, damage, armor and rewards',
      'A spyglass and four Remnant Shards'],
    art: `<div style="position:relative;height:700px">
      ${framed(region('lens-hud.jpg', 400, 240, 1120, 630, 760), 'position:absolute;left:0;top:0')}
      ${framed(region('lens-scope.jpg', 1240, 250, 660, 600, 430), 'position:absolute;right:0;bottom:0')}
      ${label('Held', 'left:20px;top:20px')}${label('Through the scope', 'right:20px;bottom:20px')}</div>` }),

  'card-rewards': () => card({
    kicker: 'Spoils', title: 'Worth the<br>risk',
    points: ['Experience grows with level and rank', 'Elites and up roll their loot again', 'Remnant Shards from every ranked kill',
      'Extra loot and shards need a player in the kill'],
    art: `<table class="grid"><tr><th>RANK</th><th>EXPERIENCE</th><th>EXTRA LOOT</th><th>SHARDS</th><th>VALOR</th></tr>
      ${RANKS.slice(1).map((r) => `<tr><td style="color:${r.color}">${icon(r.icon, r.color, 26, 'margin:0 12px 5px 0')}${r.name}</td><td>${r.xp}</td><td>${r.loot}</td><td>${r.shards}</td><td>${r.valor} + level</td></tr>`).join('')}</table>
      <div style="font-size:23px;color:var(--dim);margin:14px 4px 22px">Experience also grows 4% per level. Valor needs RPG Attribute System.</div>
      ${framed(region('valor.jpg', 480, 470, 960, 440, 978))}` }),

  'card-ras': () => card({
    kicker: 'RPG Attribute System', title: 'Better with<br>RAS',
    points: ['Mob levels follow your RAS level', 'Plates compare every mob with you', 'Ranked kills pay bonus Valor',
      'Punch up for ×1.5, farm far below for ×0.25', 'Remnant Shards absorb into Valor'],
    art: framed(region('valor.jpg', 0, 200, 1920, 720, 978)) + `<div style="margin-top:22px">${challengeChart()}</div>` }),

  'card-moon': () => card({
    kicker: 'Full moons', title: 'The Hunter’s<br>Moon',
    points: ['Some full moons rise as a Hunter’s Moon', 'Until dawn, mobs spawn five levels higher', 'Ranks are twice as likely',
      'Shards drop twice as often', 'A toll and a title warn everyone'],
    art: framed(region('hunters-moon.jpg', 0, 0, 1920, 1080, 978), '', '#c8423a') +
      stats([['Chance', '35%'], ['Levels', '+5'], ['Ranks', '×2'], ['Shards', '×2']], '#c8423a') }),

  'card-brand': () => card({
    kicker: 'Brand of Challenge', title: 'Raise the<br>stakes',
    points: ['Mark any leveled mob', 'It rises one rank and five levels', 'It gains at least one trait, heals and comes for you',
      'Every reward it drops is doubled', 'Eight Remnant Shards and an echo shard'],
    art: framed(region('brand.jpg', 0, 0, 1920, 1080, 978), '', '#b57beb') +
      stats([['Rank', '+1'], ['Levels', '+5'], ['Traits', '+1'], ['Rewards', '×2']], '#b57beb') }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Every number<br>is yours',
    points: ['Five commented JSON files', 'Bands, structures, attributes and rank odds', 'Every trait’s numbers, weight and minimum rank',
      'Missing keys come back; /remnants reload applies'],
    art: `<div style="display:flex;flex-direction:column;gap:24px">${[
      ['config/remnant_mobs/levels.json', [['"minecraft:the_nether"', '{ "min": 15, "max": 80 }'],
        ['"blocks_per_level"', '128'], ['"minecraft:ancient_city"', '40'], ['"players"', '{ "source": "ras", "weight": 0.35 }']]],
      ['config/remnant_mobs/traits.json', [['"blinking"', '{ "weight": 8, "min_rank": "elite",'], ['', '  "chance": 0.3, "cooldown_seconds": 5, "range": 8 }'],
        ['"volatile"', '{ "power": 2.5, "fuse_seconds": 1.5 }']]]]
      .map(([file, rows]) => `<div class="frame" style="background:rgba(12,10,9,.92);padding:24px 36px;font-family:monospace;font-size:23px;line-height:1.5;color:var(--text)">
        <div style="color:var(--dim)">${file}</div>
        <pre style="margin:10px 0 0;font-family:inherit">${rows.map(([k, v]) => `  ${k ? `<span style="color:var(--gold)">${k}</span>: ` : '    '}<span style="color:#9fc4ff">${v}</span>`).join('\n')}
  <span style="color:var(--dim)">…</span></pre></div>`).join('')}</div>` }),
};

const headers = { 'header-levels': 'Levels', 'header-ranks': 'Ranks', 'header-traits': 'Traits', 'header-plates': 'Nameplates',
  'header-rewards': 'Rewards', 'header-ras': 'RPG Attribute System', 'header-items': 'Items', 'header-moon': 'The Hunter’s Moon',
  'header-config': 'Configuration', 'header-everything': 'Everything Else', 'header-upgrading': 'Coming from 2.x',
  'header-compatibility': 'Downloads & Compatibility', 'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:18px">
  <div class="rule" style="width:520px;transform:scaleX(-1)"></div><span class="diamond" style="margin:0"></span><div class="rule" style="width:520px"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch();
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.waitForFunction(() => document.body.dataset.icons === 'ready');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
