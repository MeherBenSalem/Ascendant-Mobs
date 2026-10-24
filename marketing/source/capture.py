#!/usr/bin/env python3
"""Captures the listing screenshots in marketing/images/screenshots/ from a live 26.3 Fabric dev server and client.

Start them first, with RPG Attribute System in both run folders' mods/ (see marketing/README.md):
  QA_LOGS=/tmp/remnants-shots tools/qa/devrun.sh start-server 26.3 fabric
  QA_LOGS=/tmp/remnants-shots tools/qa/devrun.sh start-client 26.3 fabric
then run:
  python3 marketing/source/capture.py [shot...]

The world is a fresh "showcase" world with seed `remnants-showcase` (server.properties), so the places below are
reproducible. Every mob is real: spawned with /remnants spawn, frozen with NoAI so it holds still, and leveled,
ranked and drawn by the mod. Captures are 1920x1080 frames of the X display, saved as JPEG.

The client needs `"hide_with_hud": false` in config/remnant_mobs/client.json (plates stay up in the HUD-less shots)
and Brightness set to Bright (`gamma:1.0` in options.txt) for the Nether shot. Run `ras add level RemnantHunter 24`
once in a new world, so the hunter is RAS level 25.
"""
from __future__ import annotations

import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools' / 'qa'))
from client import PLAYER, Session  # noqa: E402

OUT = ROOT / 'marketing' / 'images' / 'screenshots'
s: Session


def c(*commands):
    return s.cmd(*commands)


def save(name):
    """Captures the screen as a JPEG in screenshots/."""
    png = OUT / f'.{name}.png'
    s.shot(png)
    from PIL import Image
    Image.open(png).convert('RGB').save(OUT / f'{name}.jpg', quality=90, optimize=True)
    png.unlink()
    print('captured', name)


def clear():
    # Strip Remnants' data first, so killing last shot's Remnant does not announce its death in chat.
    c('remnants set @e[tag=shot] clear')
    c('kill @e[type=!minecraft:player]')
    time.sleep(0.3)
    c('kill @e[type=minecraft:item]', 'kill @e[type=minecraft:experience_orb]')


def spawn(entity, x, y, z, level, rank='common', traits='', yaw=0.0, tag='shot'):
    """A leveled mob that holds still, facing yaw."""
    c(f'execute positioned {x} {y} {z} rotated {yaw} 0 run remnants spawn {entity} {level} {rank} {traits}'.rstrip())
    sel = f'@e[type={entity},x={x},y={y},z={z},distance=..1.5,limit=1,sort=nearest]'
    c(f'data merge entity {sel} {{NoAI:1b,Rotation:[{yaw}f,0f]}}', f'tag {sel} add {tag}')
    # Illagers can spawn as patrol captains with a banner on their head; nothing here should wear a helmet.
    c(f'item replace entity {sel} armor.head with minecraft:air')
    return sel


def ground(x, z, under=None, dimension='minecraft:overworld'):
    """The y a mob standing at x z would have (top of the highest block, or the highest below `under`)."""
    run = f'execute in {dimension} run '
    c(f'{run}summon minecraft:marker {x} {under or 300} {z} {{Tags:["probe"]}}')
    c(f'{run}spreadplayers {x} {z} 0 1 {f"under {under} " if under else ""}false @e[tag=probe,limit=1]')
    out = c('data get entity @e[tag=probe,limit=1] Pos')
    c('kill @e[tag=probe]')
    return float(out.split('[')[1].split(',')[1].strip().rstrip('d'))


def ahead(px, pz, yaw, forward, right=0.0):
    """The point `forward` blocks ahead of (px, pz) when facing yaw, and `right` blocks to its right."""
    import math
    rad = math.radians(yaw)
    fx, fz = -math.sin(rad), math.cos(rad)
    rx, rz = -fz, fx
    return round(px + fx * forward + rx * right, 1), round(pz + fz * forward + rz * right, 1)


def setup():
    for rule in ('spawn_mobs false', 'advance_time false', 'advance_weather false', 'show_death_messages false',
                 'send_command_feedback true'):
        c(f'gamerule {rule}')
    # Night Vision keeps the sunset scenes evenly lit (the grove is dim at this hour).
    c('weather clear', 'difficulty normal', f'gamemode creative {PLAYER}', f'effect give {PLAYER} night_vision infinite 0 true')
    c(f'clear {PLAYER}')
    # Earn the advancements up front, so no toast pops up mid-shot (the advancements shot shows them all).
    for rule in ('show_advancement_messages false', 'announce_advancements false'):
        c(f'gamerule {rule}')
    if 'RemnantHunter' not in c(f'advancement grant {PLAYER} from remnant_mobs:root'):
        return
    time.sleep(12)


SHOTS = {}


def shot(fn):
    SHOTS[fn.__name__] = fn
    return fn


GROVE = (56.5, 300.5, 70.0)  # a cherry grove 260 blocks from spawn, looking west into the sunset


def lineup(mobs, eye=GROVE, forward=8.0, spacing=2.6, time_of_day=972700):
    """Mobs in a row across the view, facing the camera. mobs: (entity, level, rank, traits)."""
    px, pz, yaw = eye
    c(f'time set {time_of_day}')
    py = ground(px, pz)
    s.tp(px, py, pz, yaw=yaw, pitch=4)
    count = len(mobs)
    for i, (entity, level, rank, traits) in enumerate(mobs):
        x, z = ahead(px, pz, yaw, forward, (i - (count - 1) / 2) * spacing)
        spawn(entity, x, ground(x, z), z, level, rank, traits, yaw=yaw + 180)
    return px, py, pz


@shot
def ranks():
    """Common to Remnant in a row. The hunter is RAS level 25, so the levels run grey, green, white, orange and red."""
    s.hud(False)
    lineup([('minecraft:pillager', 12, 'common', ''), ('minecraft:husk', 18, 'veteran', 'swift'),
            ('minecraft:vindicator', 26, 'elite', 'vampiric crushing'),
            ('minecraft:evoker', 38, 'champion', 'frostbite berserk warded'),
            ('minecraft:wither_skeleton', 52, 'remnant', 'infernal juggernaut regenerating packleader')],
           forward=7.0, spacing=3.1)
    time.sleep(6)
    save('ranks')


def hold(item):
    c(f'clear {PLAYER}', f'item replace entity {PLAYER} weapon.mainhand with {item}')


def face(eye, forward, pitch=0.0, time_of_day=972700):
    """Stands at eye looking along its yaw; returns the point `forward` blocks ahead and its ground y."""
    px, pz, yaw = eye
    c(f'time set {time_of_day}')
    s.tp(px, ground(px, pz), pz, yaw=yaw, pitch=pitch)
    x, z = ahead(px, pz, yaw, forward)
    return x, ground(x, z), z


@shot
def remnant():
    """A Remnant up close, with its boss bar, the RAS HUD and a sword in hand."""
    hold('minecraft:netherite_sword')
    x, y, z = face(GROVE, 4.6, pitch=-10)
    spawn('minecraft:wither_skeleton', x, y, z, 54, 'remnant', 'infernal juggernaut regenerating packleader', yaw=GROVE[2] + 195)
    time.sleep(6)
    save('remnant')


@shot
def plate():
    """A Champion's plate close up, a moment after a heavy hit: the pale damage trail is still draining."""
    s.hud(False)
    x, y, z = face(GROVE, 3.8, pitch=-9)
    sel = spawn('minecraft:vindicator', x, y, z, 34, 'champion', 'vampiric frostbite berserk', yaw=GROVE[2] + 180)
    time.sleep(5)
    c(f'damage {sel} 90 minecraft:player_attack by {PLAYER}')
    # The trail shows for about half a second once the client sees the hit; plate.jpg is the best of this burst.
    for i in range(4):
        time.sleep(0.15)
        save(f'plate-{i}')
    s.hud(True)


def scene(eye=GROVE):
    """A few ranked mobs scattered across the meadow in front of the camera."""
    px, pz, yaw = eye
    under = int(ground(px, pz)) + 2  # below the cherry canopy
    for entity, level, rank, traits, forward, right in (
            ('minecraft:husk', 19, 'veteran', 'venomous', 8, -5), ('minecraft:vindicator', 27, 'elite', 'swift thornskin', 10, 2.5),
            ('minecraft:pillager', 17, 'common', '', 7, 5.5), ('minecraft:wither_skeleton', 36, 'champion', 'infernal warded blinking', 13, -8)):
        x, z = ahead(px, pz, yaw, forward, right)
        spawn(entity, x, ground(x, z, under=under), z, level, rank, traits, yaw=yaw + 180 + right * 4)


@shot
def lens_hud():
    """Holding the Hunter's Lens: the threat readout under the crosshair."""
    hold('remnant_mobs:hunters_lens')
    face(GROVE, 10, pitch=2)
    scene()
    time.sleep(6)
    save('lens-hud')


@shot
def lens_scope():
    """Looking through the Hunter's Lens at a Champion: its stat sheet."""
    hold('remnant_mobs:hunters_lens')
    x, y, z = face(GROVE, 9)
    spawn('minecraft:wither_skeleton', x, y, z, 36, 'champion', 'infernal warded blinking', yaw=GROVE[2] + 180)
    s.tp(GROVE[0], ground(GROVE[0], GROVE[1]), GROVE[1], facing=(x, y + 1.4, z))
    time.sleep(4)
    s.focus()
    s.xdo('mousedown', '3')
    time.sleep(3)
    save('lens-scope')
    s.xdo('mouseup', '3')


@shot
def valor():
    """An Elite falls: RAS's level bar top left, the bonus Valor above the hotbar."""
    hold('minecraft:netherite_sword')
    x, y, z = face(GROVE, 3.2, pitch=12)
    sel = spawn('minecraft:vindicator', x, y, z, 31, 'elite', 'vampiric crushing', yaw=GROVE[2] + 180)
    time.sleep(4)
    c(f'damage {sel} 9999 minecraft:player_attack by {PLAYER}')
    time.sleep(0.5)
    save('valor')


@shot
def hunters_moon():
    """A Hunter's Moon rises over the grove on a full-moon night."""
    hold('minecraft:netherite_sword')
    eye = (GROVE[0], GROVE[1], -100.0)
    face(eye, 10, pitch=-9, time_of_day=974200)
    c('remnants moon stop')
    scene(eye)
    time.sleep(5)
    c('remnants moon start')
    time.sleep(1.2)
    save('hunters-moon')
    c('remnants moon stop')


@shot
def brand():
    """A Brand of Challenge lands: one rank up, five levels, a new trait, and it glows."""
    hold('remnant_mobs:brand_of_challenge')
    x, y, z = face(GROVE, 2.4)  # within reach (3 blocks)
    # Tall grass would catch the click before the mob does.
    bx, by, bz = int(x // 1), int(y), int(z // 1)
    for plant in ('minecraft:tall_grass', 'minecraft:short_grass'):
        c(f'fill {bx - 2} {by} {bz - 2} {bx + 2} {by + 1} {bz + 2} minecraft:air replace {plant}')
    spawn('minecraft:vindicator', x, y, z, 24, 'elite', 'vampiric crushing', yaw=GROVE[2] + 180)
    s.tp(GROVE[0], ground(GROVE[0], GROVE[1]), GROVE[1], facing=(x, y + 1.5, z))
    time.sleep(4)
    s.click(3)
    time.sleep(0.7)
    save('brand')


@shot
def commands():
    """/remnants threat and /remnants inspect in chat."""
    hold('minecraft:netherite_sword')
    x, y, z = face(GROVE, 4.0, pitch=10)
    spawn('minecraft:wither_skeleton', x, y, z, 36, 'champion', 'infernal warded blinking', yaw=GROVE[2] + 180)
    time.sleep(3)
    s.chat('/remnants threat')
    time.sleep(1)
    s.chat('/remnants inspect')
    time.sleep(1.5)
    save('commands')


@shot
def nether():
    """A Nether fortress bridge 340 blocks out (2,700 in Overworld blocks): the Nether band starts at 15 and climbs fast."""
    s.hud(False)
    c(f'effect clear {PLAYER}')
    px, py, pz, yaw = -251.5, 57, 236.5, 90.0
    c(f'execute in minecraft:the_nether run tp {PLAYER} {px} {py} {pz} {yaw} 6')
    time.sleep(6)
    # Mobs of an earlier take were saved with their chunks; they are loaded again now.
    c(f'execute in minecraft:the_nether positioned {px} {py} {pz} run kill @e[type=!minecraft:player,distance=..48]')
    time.sleep(0.5)
    c(f'execute in minecraft:the_nether positioned {px} {py} {pz} run kill @e[type=minecraft:item,distance=..48]')
    for entity, level, rank, traits, forward, right in (
            ('minecraft:wither_skeleton', 46, 'champion', 'crushing berserk warded', 5.5, 0.1),
            ('minecraft:blaze', 44, 'elite', 'deflecting volatile', 8.0, 1.4),
            ('minecraft:wither_skeleton', 43, 'veteran', 'swift', 12, -1.0)):
        x, z = ahead(px, pz, yaw, forward, right)
        y = py  # the bridge is level
        c(f'execute in minecraft:the_nether positioned {x} {y} {z} run remnants spawn {entity} {level} {rank} {traits}')
        sel = f'@e[type={entity},x={x},y={y},z={z},distance=..1.5,limit=1,sort=nearest]'
        c(f'execute in minecraft:the_nether run data merge entity {sel} {{NoAI:1b,Rotation:[{yaw + 180}f,0f]}}',
          f'execute in minecraft:the_nether run tag {sel} add shot')
    time.sleep(6)
    save('nether')
    s.hud(True)
    c(f'effect give {PLAYER} night_vision infinite 0 true', f'execute in minecraft:overworld run tp {PLAYER} {GROVE[0]} 110 {GROVE[1]}')


def main():
    global s
    s = Session()
    setup()
    wanted = sys.argv[1:] or list(SHOTS)
    try:
        for name in wanted:
            clear()
            SHOTS[name]()
            time.sleep(0.5)
    finally:
        # The next run starts from a visible HUD.
        s.hud(True)


if __name__ == '__main__':
    main()
