"""The remnant_mobs:icons bitmap font: 8x8 grey-scale glyphs that text colour tints (rank crests, traits, HUD icons).

'#' is full brightness, 'o' a slightly darker fill, '+' and ':' darker shades, '.' transparent.
"""
from __future__ import annotations

from PIL import Image

SHADES = {'#': 255, 'o': 205, '+': 160, ':': 110}

CRESTS = {
    0xE000: """
.......
.......
...#...
..#o#..
...#...
.......
.......""",
    0xE001: """
.......
...#...
..#o#..
.#o.o#.
#o...o#
.......
.......""",
    0xE002: """
...#...
..#o#..
.#o.o#.
#..#..#
..#o#..
.#o.o#.
#o...o#""",
    0xE003: """
.......
#..#..#
#o.#.o#
#o###o#
#ooooo#
#######
.......""",
    0xE004: """
#.....#
#.###.#
.#ooo#.
.#:o:#.
.#ooo#.
..#.#..
.......""",
}

TRAITS = [
    # vampiric: blood drop
    """
...#...
..#o#..
..#o#..
.#ooo#.
.#o+o#.
.#+++#.
..###..""",
    # frostbite: snowflake
    """
...#...
.#.#.#.
..###..
###o###
..###..
.#.#.#.
...#...""",
    # infernal: flame
    """
...#...
..#o...
..#o#..
.#oo+#.
.#o+o#.
.#+:+#.
..###..""",
    # venomous: bubbles
    """
..##...
.#oo#..
.#oo#.#
..##.#o
.##...#
#oo#...
.##....""",
    # withering: wither skull
    """
.#####.
#ooooo#
#o:o:o#
#ooooo#
.#o#o#.
.#####.
.......""",
    # berserk: crossed axes
    """
##...##
#o#.#o#
.#o#o#.
..#o#..
.#.#.#.
#.....#
.......""",
    # crushing: hammer
    """
#####..
#ooo#..
#####..
..#o...
..#o...
..#o...
..##...""",
    # juggernaut: helm
    """
.#####.
#ooooo#
#o###o#
#o#.#o#
#ooooo#
.#o#o#.
.#####.""",
    # thornskin: thorn vine
    """
.....#.
..#.#o.
.#o#o#.
#o#o#..
.#o#.#.
#o#....
.#.....""",
    # warded: shield with cross
    """
#######
#ooooo#
#oo#oo#
#o###o#
#oo#oo#
.#ooo#.
..###..""",
    # regenerating: cross
    """
..###..
..#o#..
###o###
#ooooo#
###o###
..#o#..
..###..""",
    # deflecting: arrow off a wall
    """
.....##
....#o#
...#o#.
#.#o#..
#o#o...
#.#....
#......""",
    # swift: speed lines
    """
.......
####...
...###.
######o
...###.
####...
.......""",
    # blinking: eye
    """
.......
..###..
.#ooo#.
#o#:#o#
.#ooo#.
..###..
.......""",
    # packleader: paw
    """
.#...#.
#o#.#o#
.#...#.
..###..
.#ooo#.
.#ooo#.
..###..""",
    # volatile: bomb
    """
.....#.
....#..
..###..
.#ooo#.
#oo+oo#
#o+++o#
.#####.""",
]

MISC = {
    0xE020: """
.#####.
#ooooo#
#o:o:o#
#ooooo#
.#ooo#.
.#.#.#.
.......""",
    0xE021: """
..###..
.#oo...
#oo....
#oo....
#oo....
.#oo...
..###..""",
    0xE022: """
.#####.
#..#..#
#.#o#.#
#.#o#.#
#..#..#
.#####.
.......""",
    0xE023: """
.##.##.
#oo#oo#
#ooooo#
.#ooo#.
..#o#..
...#...
.......""",
    0xE024: """
......#
.....#o
....#o.
#..#o..
.##o...
.#o#...
#..#...""",
    0xE025: """
#######
#ooooo#
#ooooo#
#ooooo#
.#ooo#.
..#o#..
...#...""",
    0xE026: """
.###...
#ooo#..
#o.o#..
#ooo#..
.###o..
....#o.
.....#o""",
}


def glyph(art: str) -> Image.Image:
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    rows = art.strip('\n').split('\n')
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in SHADES:
                v = SHADES[ch]
                img.putpixel((x, y), (v, v, v, 255))
    return img


def glyphs() -> dict[int, Image.Image]:
    out = {cp: glyph(art) for cp, art in CRESTS.items()}
    for i, art in enumerate(TRAITS):
        out[0xE010 + i] = glyph(art)
    out.update({cp: glyph(art) for cp, art in MISC.items()})
    return out


def sheet():
    """A 16x3 grid of 8x8 cells: row 0 crests (E000-), row 1 traits (E010-), row 2 misc (E020-)."""
    img = Image.new('RGBA', (128, 24), (0, 0, 0, 0))
    rows = [[chr(0) for _ in range(16)] for _ in range(3)]
    for cp, g in glyphs().items():
        row = (cp - 0xE000) // 16
        col = (cp - 0xE000) % 16
        img.alpha_composite(g, (col * 8, row * 8))
        rows[row][col] = chr(cp)
    return img, [''.join(r) for r in rows]


def font_json(namespace: str):
    _, chars = sheet()
    return {'providers': [{'type': 'bitmap', 'file': f'{namespace}:font/icons.png', 'ascent': 7, 'height': 8, 'chars': chars}]}
