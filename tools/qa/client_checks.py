#!/usr/bin/env python3
"""In-game checks with a real QA client online (see devrun.sh): nameplates of every rank, the damage trail, the
Hunter's Lens readout and stat sheet, the Brand of Challenge, Remnant Shards, the Remnant boss bar and the Hunter's
Moon. Screenshots go to --out for a visual pass.

  python3 tools/qa/client_checks.py --out /tmp/remnants-qa/shots [--ras]
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from client import Session, PLAYER  # noqa: E402

FAILURES = []


def check(name, ok, detail=''):
    print(('PASS ' if ok else 'FAIL ') + name + (f'  [{str(detail)[:300]}]' if detail and not ok else ''))
    if not ok:
        FAILURES.append(name)


def number(text, default=None):
    found = re.findall(r'-?\d+(?:\.\d+)?', text)
    return float(found[-1]) if found else default


def valor(c):
    for root in ('neoforge:attachments', 'fabric:attachments', 'ForgeCaps', 'cardinal_components'):
        out = c(f'data get entity {PLAYER} "{root}"."rpg_attribute_system:player_variables".vars.totalXp')
        found = re.search(r'entity data: (-?\d+(?:\.\d+)?)d', out)
        if found:
            return float(found.group(1))
    found = re.search(r'totalXp: (-?\d+(?:\.\d+)?)d', c(f'data get entity {PLAYER}'))
    return float(found.group(1)) if found else -1.0


def data_tag(c, selector):
    found = re.search(r'"remnant_mobs:data\|([^"]+)"', c(f'data get entity {selector} Tags'))
    return found.group(1).split('|') if found else None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', required=True)
    ap.add_argument('--ras', action='store_true')
    ap.add_argument('--mc', default='26')
    args = ap.parse_args()
    out = Path(args.out)
    s = Session()
    c = s.cmd
    legacy_rules = not args.mc.startswith('26')
    for cmd in ('gamerule doDaylightCycle false' if legacy_rules else 'gamerule advance_time false',
                'time set 13400', 'weather clear', 'remnants moon stop', f'gamemode creative {PLAYER}', f'clear {PLAYER}',
                f'effect give {PLAYER} minecraft:resistance infinite 5 true', 'kill @e[type=!minecraft:player]',
                'forceload add 0 0 48 48', 'fill 0 120 0 40 120 40 minecraft:polished_deepslate',
                'fill 0 121 0 40 135 40 minecraft:air', 'gamerule spawn_mobs false' if not legacy_rules else 'gamerule doMobSpawning false'):
        c(cmd)
    s.click(1)
    s.wait(1)

    # A row of mobs, one per rank.
    row = [('zombie', 4, 'common', ''), ('skeleton', 12, 'veteran', 'swift'), ('zombie', 25, 'elite', 'vampiric frostbite'),
           ('wither_skeleton', 41, 'champion', 'infernal berserk warded'), ('zombie', 58, 'remnant', 'crushing blinking regenerating venomous')]
    for i, (mob, level, rank, traits) in enumerate(row):
        x = 8.5 + i * 3
        c(f'summon minecraft:{mob} {x} 121 20.5 {{NoAI:1b,Rotation:[180f,0f],Tags:["qa_row{i}"]}}')
        s.wait(0.3)
        sel = f'@e[tag=qa_row{i},limit=1]'
        c(f'remnants set {sel} level {level}')
        c(f'remnants set {sel} rank {rank}')
        c(f'remnants set {sel} traits {traits or "none"}')
        c(f'data merge entity {sel} {{Health:1000f}}')
    s.wait(1)
    s.tp(14.5, 121, 11.5, facing=(14.5, 122.6, 20.5))
    s.hud(False)
    s.wait(2)
    s.shot(out / 'plates-row.png')

    # Damage trail: hit the elite and catch the bar mid-drain.
    c(f'damage @e[tag=qa_row2,limit=1] 18 minecraft:generic')
    s.wait(0.25)
    s.tp(14.5, 121, 16.5, facing=(14.5, 123, 20.5))
    s.wait(0.2)
    s.shot(out / 'plates-trail.png')
    s.wait(2)
    s.shot(out / 'plates-close.png')
    s.hud(True)

    # Hunter's Lens: area readout when held.
    c(f'item replace entity {PLAYER} hotbar.0 with remnant_mobs:hunters_lens')
    s.key('1')
    s.tp(14.5, 121, 11.5, facing=(14.5, 122.6, 20.5))
    s.wait(2.5)
    s.shot(out / 'lens-held.png')
    # Scope on the Champion.
    s.tp(17.5, 121, 9.5, facing=(17.5, 122.4, 20.5))
    s.wait(1)
    s.focus()
    s.xdo('mousedown', '3')
    s.wait(1.5)
    s.shot(out / 'lens-scope.png')
    s.xdo('mouseup', '3')
    s.wait(0.5)
    adv = c(f'execute if entity @a[name={PLAYER},advancements={{remnant_mobs:know_thy_enemy=true}}]')
    check('lens grants Know Thy Enemy', 'passed' in adv.lower(), adv)

    # Brand of Challenge on the Veteran skeleton (survival: items are used up).
    c(f'gamemode survival {PLAYER}')
    c(f'item replace entity {PLAYER} hotbar.1 with remnant_mobs:brand_of_challenge 2')
    s.key('2')
    s.tp(11.5, 121, 17.5, facing=(11.5, 122.3, 20.5))
    s.wait(1)
    s.click(3)
    s.wait(1)
    d = data_tag(c, '@e[tag=qa_row1,limit=1]')
    check('brand raises rank, level and traits', d is not None and d[1] == 'elite' and d[0] == '17' and int(d[4]) & 1
          and len(d[2].split('+')) >= 2, d)
    s.shot(out / 'brand.png')

    # Remnant Shards: one, then the stack while sneaking.
    c(f'xp set {PLAYER} 0 levels')
    c(f'xp set {PLAYER} 0 points')
    c(f'item replace entity {PLAYER} hotbar.2 with remnant_mobs:remnant_shard 5')
    s.key('3')
    s.tp(14.5, 121, 5.5, 0, -60)
    s.wait(1)
    valor_before = valor(c)
    s.click(3)
    s.wait(1)
    inv = c(f'data get entity {PLAYER} Inventory')
    left = re.search(r'remnant_mobs:remnant_shard"[^}]*?count: (\d+)|count: (\d+)[^}]*?remnant_mobs:remnant_shard', inv)
    count = int(next(g for g in left.groups() if g)) if left else 0
    check('using a shard absorbs one', count == 4, inv[:300])
    if args.ras:
        gained = valor(c) - valor_before
        check('shard grants 15 Valor', abs(gained - 15) < 0.01, gained)
    else:
        check('shard grants experience', number(c(f'xp query {PLAYER} points'), 0) > 0 or number(c(f'xp query {PLAYER} levels'), 0) > 0)
    s.focus()
    s.xdo('keydown', 'shift')
    s.wait(0.3)
    s.click(3)
    s.wait(0.5)
    s.xdo('keyup', 'shift')
    inv = c(f'data get entity {PLAYER} Inventory')
    check('sneaking absorbs the stack', 'remnant_mobs:remnant_shard' not in inv, inv[:300])

    # The Remnant's boss bar and the Hunter's Moon.
    c(f'gamemode creative {PLAYER}')
    s.tp(20.5, 121, 13.5, facing=(20.5, 122.6, 20.5))
    s.wait(2)
    s.shot(out / 'remnant-bar.png')
    c('remnants moon start')
    s.wait(2.5)
    s.shot(out / 'moon-title.png')
    s.wait(5)
    s.shot(out / 'moon-hud.png')
    c('remnants moon stop')

    c('kill @e[type=!minecraft:player]')
    c('gamerule spawn_mobs true' if not legacy_rules else 'gamerule doMobSpawning true')
    print(f'\n{len(FAILURES)} failure(s)' + (': ' + ', '.join(FAILURES) if FAILURES else ''))
    sys.exit(1 if FAILURES else 0)


if __name__ == '__main__':
    main()
