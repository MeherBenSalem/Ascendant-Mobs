#!/usr/bin/env python3
"""Server-side QA for Remnants: RPG Mobs 3.0, driven over RCON against a running dev server.

  python3 tools/qa/server_smoke.py --run-dir 26.3/fabric/runs/server [--ras] [--with-player]

Checks: config files, threat at several places, leveling of new mobs, explicit spawns (stats per level, rank and
trait), commands (set, reroll, clear, inspect), bosses and passive mobs, 2.x mob conversion, Packleader, Volatile,
the Hunter's Moon, rewards (experience, extra loot, shards) for player kills, and config reloads.
--with-player needs a QA client online (RemnantHunter); --ras expects RPG Attribute System to be installed.
"""
from __future__ import annotations

import argparse
import re
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from rcon import wait_for  # noqa: E402

FAILURES = []
PLAYER = 'RemnantHunter'


def check(name, ok, detail=''):
    print(('PASS ' if ok else 'FAIL ') + name + (f'  [{str(detail)[:400]}]' if detail and not ok else ''))
    if not ok:
        FAILURES.append(name)


def number(text, default=None):
    found = re.findall(r'-?\d+(?:\.\d+)?', text)
    return float(found[-1]) if found else default


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--run-dir', required=True)
    ap.add_argument('--port', type=int, default=25575)
    ap.add_argument('--with-player', action='store_true')
    ap.add_argument('--ras', action='store_true')
    ap.add_argument('--mc', default='26')
    args = ap.parse_args()
    run = Path(args.run_dir)
    r = wait_for(args.port)
    c = r.command

    def wait(seconds):
        time.sleep(seconds)

    def tags(selector):
        return c(f'data get entity {selector} Tags')

    def data_tag(selector):
        found = re.search(r'"remnant_mobs:data\|([^"]+)"', tags(selector))
        return found.group(1).split('|') if found else None

    def attr(selector, attribute):
        return number(c(f'attribute {selector} {attribute} get'), -1)

    cfg = run / 'config' / 'remnant_mobs'
    for name in ('levels.json', 'ranks.json', 'traits.json', 'rewards.json', 'client.json'):
        check(f'config {name} generated', (cfg / name).exists())

    # Threat at several places (world spawn moved to the origin first).
    c('setworldspawn 0 80 0')
    out = c('execute in minecraft:overworld positioned 0 80 0 run remnants threat')
    check('threat at spawn is low', 'Threat here' in out and (number(out.split('Threat here:')[1].split('(')[0]) <= 4
                                                             or 'nearby at level' in out), out)
    out = c('execute in minecraft:overworld positioned 5000 80 5000 run remnants threat')
    far = number(out.split('Threat here:')[1].split('(')[0]) if 'Threat here' in out else -1
    check('threat grows with distance', 50 <= far <= 60, out)
    out = c('execute in minecraft:overworld positioned 0 -40 0 run remnants threat')
    deep = number(out.split('Threat here:')[1].split('(')[0]) if 'Threat here' in out else -1
    check('threat grows with depth', deep >= 6, out)
    out = c('execute in minecraft:the_nether positioned 1000 64 0 run remnants threat')
    nether = number(out.split('Threat here:')[1].split('(')[0]) if 'Threat here' in out else -1
    check('nether counts eight blocks per block', 70 <= nether <= 80, out)
    out = c('execute in minecraft:the_end positioned 0 64 0 run remnants threat')
    end = number(out.split('Threat here:')[1].split('(')[0]) if 'Threat here' in out else -1
    check('end band starts at 35', end >= 35, out)

    # Test area: lit and roofed (no natural spawns, no burning undead), with natural spawning and the clock stopped.
    modern = args.mc.startswith('26')
    c('gamerule mob_drops true' if modern else 'gamerule doMobLoot true')
    c('gamerule spawn_mobs false' if modern else 'gamerule doMobSpawning false')
    c('gamerule advance_time false' if modern else 'gamerule doDaylightCycle false')
    c('time set 6000')
    c('forceload add 0 0 48 48')
    c('fill 0 120 0 40 120 40 minecraft:sea_lantern')
    c('fill 0 121 0 40 130 40 minecraft:air')
    c('fill 0 126 0 40 126 40 minecraft:stone')
    c('kill @e[type=!minecraft:player]')
    if args.with_player:
        # Mobs further than 128 blocks from every player despawn at once: keep the player by the test area.
        c(f'gamemode creative {PLAYER}')
        c(f'tp {PLAYER} 20 122 36')

    # A plain summon gets leveled on its first tick.
    c('summon minecraft:zombie 4 121 4 {NoAI:1b,Tags:["qa_plain"]}')
    wait(1)
    d = data_tag('@e[tag=qa_plain,limit=1]')
    check('new zombie is leveled', d is not None and int(d[0]) >= 1, tags('@e[tag=qa_plain,limit=1]'))
    if d:
        level = int(d[0])
        expected = 20 * (1 + min(6 * level, 600) / 100)
        rank = d[1]
        health = attr('@e[tag=qa_plain,limit=1]', 'minecraft:max_health')
        if rank == 'common':
            check('level scales max health', abs(health - expected) < 0.01, f'{health} vs {expected}')

    # Explicit spawns: level 50 Elite skeleton with fixed traits.
    out = c('execute positioned 8 121 4 run remnants spawn minecraft:skeleton 50 elite vampiric swift')
    check('spawn command', 'Spawned' in out, out)
    wait(1)
    sel = '@e[type=minecraft:skeleton,x=8,y=121,z=4,distance=..2,limit=1]'
    d = data_tag(sel)
    check('spawned data', d is not None and d[0] == '50' and d[1] == 'elite' and set(d[2].split('+')) == {'vampiric', 'swift'}, d)
    check('elite rank tag', 'remnant_mobs.elite' in tags(sel), tags(sel))
    health = attr(sel, 'minecraft:max_health')
    check('elite level 50 health = 20 x 4 x 2', abs(health - 160) < 0.01, health)
    def modifier(attribute, mid):
        return number(c(f'attribute {sel} {attribute} modifier value get {mid}'), -1)
    check('armor modifiers: level + elite', abs(modifier('minecraft:armor', 'remnant_mobs:level') - 7.5) < 0.01
          and abs(modifier('minecraft:armor', 'remnant_mobs:rank') - 4) < 0.01)
    check('swift modifier', abs(modifier('minecraft:movement_speed', 'remnant_mobs:trait/swift') - 0.3) < 0.001)
    speed = attr(sel, 'minecraft:movement_speed')
    check('speed = base x level x swift', abs(speed - 0.25 * 1.15 * 1.3) < 0.002, speed)
    hp = number(c(f'data get entity {sel} Health'), -1)
    check('spawned at full health', abs(hp - 160) < 0.01, hp)

    # set level keeps the health ratio and re-applies stats.
    c(f'remnants set {sel} level 10')
    health = attr(sel, 'minecraft:max_health')
    check('set level re-applies stats', abs(health - 20 * 1.6 * 2) < 0.01, health)
    c(f'remnants set {sel} rank common')
    health = attr(sel, 'minecraft:max_health')
    check('set rank common drops rank stats', abs(health - 32) < 0.01, health)
    check('rank tag removed', 'remnant_mobs.elite' not in tags(sel), tags(sel))
    c(f'remnants set {sel} traits juggernaut swift')
    d = data_tag(sel)
    check('conflicting traits are dropped', d is not None and d[2] == 'juggernaut', d)
    check('juggernaut knockback resistance', attr(sel, 'minecraft:knockback_resistance') >= 1.0)
    out = c(f'remnants inspect {sel}')
    check('inspect', 'Level 10' in out or 'Level' in out, out)
    c(f'remnants set {sel} clear')
    check('clear removes data', data_tag(sel) is None and abs(attr(sel, 'minecraft:max_health') - 20) < 0.01, tags(sel))
    c(f'remnants set {sel} reroll')
    check('reroll levels again', data_tag(sel) is not None, tags(sel))

    # Remnant rank: title, persistence.
    out = c('execute positioned 12 121 4 run remnants spawn minecraft:skeleton 40 remnant')
    wait(1)
    sel = '@e[type=minecraft:skeleton,x=12,y=121,z=4,distance=..2,limit=1]'
    d = data_tag(sel)
    check('remnant spawned', d is not None and d[1] == 'remnant' and len(d[2].split('+')) == 4, d)
    check('remnant has a title', 'CustomName' in c(f'data get entity {sel}') and len(c(f'data get entity {sel} CustomName')) > 10,
          c(f'data get entity {sel} CustomName'))
    check('remnant is persistent', number(c(f'data get entity {sel} PersistenceRequired')) == 1, c(f'data get entity {sel} PersistenceRequired'))

    # Bosses keep their stats; passive animals are left alone.
    c('summon minecraft:elder_guardian 20 121 4 {NoAI:1b,Tags:["qa_boss"]}')
    c('summon minecraft:cow 24 121 4 {NoAI:1b,Tags:["qa_cow"]}')
    wait(1)
    d = data_tag('@e[tag=qa_boss,limit=1]')
    check('boss gets a level, no rank', d is not None and d[1] == 'common' and int(d[4]) & 16, d)
    check('boss stats unchanged', abs(attr('@e[tag=qa_boss,limit=1]', 'minecraft:max_health') - 80) < 0.01)
    check('cow is not leveled', data_tag('@e[tag=qa_cow,limit=1]') is None, tags('@e[tag=qa_cow,limit=1]'))

    # A 2.x mob converts.
    c('summon minecraft:husk 28 121 4 {NoAI:1b,Tags:["qa_legacy","am_level_25.0","am_got_level"],'
      'attributes:[{id:"minecraft:max_health",base:20,modifiers:[{id:"rpgmoblevelingsystem:level",amount:50.0,operation:"add_value"}]}]}')
    wait(1)
    d = data_tag('@e[tag=qa_legacy,limit=1]')
    check('2.x mob keeps its level', d is not None and d[0] == '25', tags('@e[tag=qa_legacy,limit=1]'))
    check('2.x tags removed', 'am_level_' not in tags('@e[tag=qa_legacy,limit=1]'))
    check('2.x modifier replaced', abs(attr('@e[tag=qa_legacy,limit=1]', 'minecraft:max_health') - 20 * 2.5) < 0.01,
          attr('@e[tag=qa_legacy,limit=1]', 'minecraft:max_health'))

    # Packleader calls its pack once.
    c('kill @e[type=minecraft:zombie]')
    c('execute positioned 4 121 12 run remnants spawn minecraft:zombie 20 champion packleader')
    wait(1)
    sel = '@e[type=minecraft:zombie,x=4,y=121,z=12,distance=..2,limit=1]'
    max_hp = attr(sel, 'minecraft:max_health')
    c(f'damage {sel} {max_hp * 0.5:.1f} minecraft:generic_kill')
    wait(1)
    pack = '@e[type=minecraft:zombie,tag=!remnant_mobs.champion,x=4,y=121,z=12,distance=..6]'
    minions = c(f'execute if entity {pack}')
    count = number(minions, 0) if 'passed' in minions.lower() else 0
    check('packleader called two minions', count == 2, minions)
    c(f'damage {sel} 1 minecraft:generic_kill')
    wait(1)
    minions = c(f'execute if entity {pack}')
    check('packleader calls only once', number(minions, 0) == 2, minions)
    c('kill @e[type=minecraft:zombie]')

    # Volatile explodes after death without breaking blocks.
    c('execute positioned 20 121 20 run remnants spawn minecraft:zombie 10 veteran volatile')
    c('summon minecraft:armor_stand 21 121 20 {Tags:["qa_stand"]}')
    c('kill @e[type=minecraft:zombie,x=20,y=121,z=20,distance=..2]')
    wait(3)
    check('volatile: blocks intact', 'passed' in c('execute if block 20 120 20 minecraft:sea_lantern').lower())
    stand = c('execute if entity @e[tag=qa_stand]')
    check('volatile: blast hit nearby entities', 'passed' not in stand.lower(), stand)

    # The Hunter's Moon.
    out = c('remnants moon start')
    check('moon start', "Hunter's Moon" in out, out)
    out = c('remnants moon')
    check('moon status', 'is up' in out, out)
    c('summon minecraft:zombie 30 121 30 {NoAI:1b,Tags:["qa_moon"]}')
    wait(1)
    d = data_tag('@e[tag=qa_moon,limit=1]')
    check('moonlit flag', d is not None and int(d[4]) & 32, d)
    out = c('execute positioned 0 80 0 run remnants threat')
    check('moon raises threat', 'moon 5' in out, out)
    c('remnants moon stop')
    check('moon stop', 'No Hunter' in c('remnants moon'), c('remnants moon'))

    # Reload.
    out = c('remnants reload')
    check('reload', 'Reloaded' in out, out)

    if args.with_player:
        player_checks(c, wait, tags, data_tag, attr, args)

    c('kill @e[type=!minecraft:player]')
    c('fill 0 126 0 40 126 40 minecraft:air')
    c('gamerule spawn_mobs true' if modern else 'gamerule doMobSpawning true')
    r.close()
    print(f'\n{len(FAILURES)} failure(s)' + (': ' + ', '.join(FAILURES) if FAILURES else ''))
    sys.exit(1 if FAILURES else 0)


def count_items(c, item, x, y, z, radius=6):
    out = c(f'execute as @e[type=minecraft:item,x={x},y={y},z={z},distance=..{radius}] run data get entity @s Item')
    total = 0
    for entry in re.findall(r'\{[^{}]*id: "' + re.escape(item) + r'"[^{}]*\}', out):
        found = re.search(r'\b[Cc]ount: (\d+)', entry)
        total += int(found.group(1)) if found else 1
    return total


def player_checks(c, wait, tags, data_tag, attr, args):
    c(f'gamemode survival {PLAYER}')
    c(f'tp {PLAYER} 10 121 34')
    c(f'effect give {PLAYER} minecraft:resistance infinite 4 true')
    c('kill @e[type=minecraft:item]')
    c('kill @e[type=minecraft:experience_orb]')

    # Player kill of a Champion: extra loot, shards, more experience.
    c('execute positioned 10 121 30 run remnants spawn minecraft:zombie 30 champion')
    sel = '@e[type=minecraft:zombie,x=10,y=121,z=30,distance=..2,limit=1]'
    wait(1)
    c(f'damage {sel} 100000 minecraft:player_attack by {PLAYER}')
    wait(1)
    shards = count_items(c, 'remnant_mobs:remnant_shard', 10, 121, 30)
    check('champion dropped 4-6 shards', 4 <= shards <= 6, shards)
    c('kill @e[type=minecraft:item]')
    c('kill @e[type=minecraft:experience_orb]')

    # Experience: 5 x (1 + 30 x 0.04) x 5 for a level 30 Champion zombie, picked up by the player.
    c(f'tp {PLAYER} 10 122 26')
    c(f'xp set {PLAYER} 0 levels')
    c(f'xp set {PLAYER} 0 points')
    c('execute positioned 10 121 28 run remnants spawn minecraft:zombie 30 champion')
    wait(1)
    c(f'damage @e[type=minecraft:zombie,x=10,y=121,z=28,distance=..2,limit=1] 100000 minecraft:player_attack by {PLAYER}')
    wait(4)
    levels = int(number(c(f'xp query {PLAYER} levels'), 0))
    points = int(number(c(f'xp query {PLAYER} points'), 0))
    total = (levels ** 2 + 6 * levels if levels <= 16 else 2.5 * levels ** 2 - 40.5 * levels + 360 if levels <= 31
             else 4.5 * levels ** 2 - 162.5 * levels + 2220) + points
    # The base is 5 plus 1-3 per piece of equipment the zombie spawned with.
    check('champion experience multiplied (base x 2.2 x 5)', 55 <= total <= 17 * 11, f'{levels} levels {points} points = {total}')
    c(f'tp {PLAYER} 20 122 36')
    c('kill @e[type=minecraft:item]')
    c('kill @e[type=minecraft:experience_orb]')

    # Kill without player credit: no shards.
    c('execute positioned 14 121 30 run remnants spawn minecraft:zombie 30 champion')
    c('kill @e[type=minecraft:zombie,x=14,y=121,z=30,distance=..2]')
    wait(1)
    check('no shards without a player kill', count_items(c, 'remnant_mobs:remnant_shard', 14, 121, 30) == 0)

    # Thornskin reflects melee damage.
    c(f'effect clear {PLAYER} minecraft:resistance')
    c(f'effect give {PLAYER} minecraft:instant_health 1 10 true')
    wait(1)
    before = number(c(f'data get entity {PLAYER} Health'))
    c('execute positioned 18 121 30 run remnants spawn minecraft:zombie 30 veteran thornskin')
    c(f'damage @e[type=minecraft:zombie,x=18,y=121,z=30,distance=..2,limit=1] 8 minecraft:player_attack by {PLAYER}')
    wait(1)
    after = number(c(f'data get entity {PLAYER} Health'))
    check('thornskin hurts the attacker', after < before, f'{before} -> {after}')
    c(f'effect give {PLAYER} minecraft:resistance infinite 4 true')
    c('kill @e[type=minecraft:zombie]')

    if args.ras:
        ras_checks(c, wait, args)

    # Spawners tag their mobs and roll ranks less often.
    c(f'gamemode creative {PLAYER}')
    c(f'tp {PLAYER} 8 121 12')
    c('setblock 4 121 16 minecraft:spawner{SpawnData:{entity:{id:"minecraft:zombie"},custom_spawn_rules:{block_light_limit:[0,15],'
      'sky_light_limit:[0,15]}},Delay:0s,MinSpawnDelay:20s,MaxSpawnDelay:40s,SpawnCount:2s,MaxNearbyEntities:6s,RequiredPlayerRange:64s}')
    flagged = False
    for _ in range(10):
        wait(3)
        out = c('execute as @e[type=minecraft:zombie,x=4,y=121,z=16,distance=..16] run data get entity @s Tags')
        for found in re.findall(r'"remnant_mobs:data\|([^"]+)"', out):
            if int(found.split('|')[4]) & 8:
                flagged = True
        if flagged:
            break
    check('spawner mobs are flagged', flagged, c('execute as @e[type=minecraft:zombie] run data get entity @s Tags'))
    c('setblock 4 121 16 minecraft:air')
    c('kill @e[type=minecraft:zombie]')


def total_valor(c):
    for root in ('neoforge:attachments', 'fabric:attachments', 'ForgeCaps', 'cardinal_components'):
        out = c(f'data get entity {PLAYER} "{root}"."rpg_attribute_system:player_variables".vars.totalXp')
        found = re.search(r'entity data: (-?\d+(?:\.\d+)?)d', out)
        if found:
            return float(found.group(1))
    found = re.search(r'totalXp: (-?\d+(?:\.\d+)?)d', c(f'data get entity {PLAYER}'))
    return float(found.group(1)) if found else -1.0


def ras_checks(c, wait, args):
    run = Path(args.run_dir)
    c(f'ras set xp 0 {PLAYER}')
    c(f'ras add level {PLAYER} 20')
    level = number(c(f'ras level {PLAYER}').replace('§e', ' ').replace('§a', ' ').split('level is')[-1], -1)
    check('RAS level set to 20', level == 20, level)
    out = c(f'execute as {PLAYER} at @s run remnants threat')
    check('threat follows the RAS level', 'level 20' in out, out)

    # Bonus Valor for a ranked kill: make it unmistakable (10000 per Remnant) and compare.
    rewards = run / 'config' / 'remnant_mobs' / 'rewards.json'
    original = rewards.read_text(encoding='utf-8')
    try:
        rewards.write_text(original.replace('"remnant": 150', '"remnant": 10000'), encoding='utf-8')
        c('remnants reload')
        before = total_valor(c)
        c('execute positioned 10 121 30 run remnants spawn minecraft:zombie 22 remnant')
        wait(1)
        c(f'damage @e[type=minecraft:zombie,x=10,y=121,z=30,distance=..2,limit=1] 100000 minecraft:player_attack by {PLAYER}')
        wait(1)
        gained = total_valor(c) - before
        # (10000 + 22 x 1) x challenge(+2 levels: 1.1)
        check('ranked kill grants bonus Valor', gained >= (10000 + 22) * 1.1 - 1, f'{before} -> {gained}')
    finally:
        rewards.write_text(original, encoding='utf-8')
        c('remnants reload')
    c('kill @e[type=minecraft:item]')
    c('kill @e[type=minecraft:experience_orb]')


if __name__ == '__main__':
    main()
