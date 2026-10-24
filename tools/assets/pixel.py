"""Tiny pixel-art toolkit for the Remnants: RPG Mobs textures.

Everything is drawn on RGBA PIL images at native resolution (16x16 for items and blocks). Colours are hex strings or
RGB(A) tuples. Ramps run dark to light and are hue-shifted by hand in the palettes, never generated.
"""
from __future__ import annotations

import math
import random
from PIL import Image


def rgb(value):
    if isinstance(value, tuple):
        return value if len(value) == 4 else (*value, 255)
    value = value.lstrip('#')
    alpha = int(value[6:8], 16) if len(value) == 8 else 255
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4)) + (alpha,)


def mix(a, b, t):
    a, b = rgb(a), rgb(b)
    return tuple(int(round(x + (y - x) * t)) for x, y in zip(a, b))


def canvas(w=16, h=16, fill=(0, 0, 0, 0)):
    return Image.new('RGBA', (w, h), fill)


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height and color is not None:
        img.putpixel((x, y), rgb(color))


def get(img, x, y):
    if 0 <= x < img.width and 0 <= y < img.height:
        return img.getpixel((x, y))
    return (0, 0, 0, 0)


def rect(img, x0, y0, x1, y1, color):
    """Inclusive rectangle fill."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(img, x, y, color)


def stamp(img, x0, y0, art: str, palette: dict):
    """Draws ASCII art; '.' and ' ' are transparent (skipped)."""
    rows = [r for r in art.strip('\n').split('\n')]
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch in '. ':
                continue
            put(img, x0 + dx, y0 + dy, palette[ch])


def grid(art: str):
    return [list(r) for r in art.strip('\n').split('\n')]


def outline(img, color, diagonal=False):
    """Adds a 1px outline around opaque pixels (outside them)."""
    src = img.copy()
    neighbours = [(-1, 0), (1, 0), (0, -1), (0, 1)]
    if diagonal:
        neighbours += [(-1, -1), (1, -1), (-1, 1), (1, 1)]
    for y in range(img.height):
        for x in range(img.width):
            if src.getpixel((x, y))[3] != 0:
                continue
            if any(src.getpixel((x + dx, y + dy))[3] != 0 for dx, dy in neighbours
                   if 0 <= x + dx < img.width and 0 <= y + dy < img.height):
                put(img, x, y, color)


def scale(img, factor):
    return img.resize((img.width * factor, img.height * factor), Image.NEAREST)


def sheet(images, factor=8, columns=None, gap=2, bg=(36, 36, 44, 255)):
    columns = columns or len(images)
    rows = math.ceil(len(images) / columns)
    w = max(i.width for i in images) * factor
    h = max(i.height for i in images) * factor
    out = Image.new('RGBA', (columns * (w + gap * factor) + gap * factor, rows * (h + gap * factor) + gap * factor), bg)
    for n, image in enumerate(images):
        cx, cy = n % columns, n // columns
        out.alpha_composite(scale(image, factor), (gap * factor + cx * (w + gap * factor), gap * factor + cy * (h + gap * factor)))
    return out


class Rng(random.Random):
    """Seeded randomness; textures are reproducible."""

    def __init__(self, seed):
        super().__init__(seed)


def strip(frames):
    """Stacks animation frames vertically (Minecraft animated texture layout)."""
    out = Image.new('RGBA', (frames[0].width, frames[0].height * len(frames)), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        out.alpha_composite(frame, (0, i * frame.height))
    return out
