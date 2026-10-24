"""Original sound effects for Remnants: RPG Mobs, synthesised from noise and partials, then encoded to mono OGG."""
from __future__ import annotations

import numpy as np

from synth import (RATE, bandpass, bell, env_adsr, exp_decay, highpass, lowpass, normalize, note, place, shimmer,
                   t_axis, write_ogg)  # noqa: F401


def brass(freq, seconds, seed, vibrato=4.5, brightness=2400):
    """A rough horn: band-limited sawtooth with breath noise and slow vibrato."""
    rng = np.random.default_rng(seed)
    t = t_axis(seconds)
    vib = 1 + 0.006 * np.sin(2 * np.pi * vibrato * t) * np.minimum(1, t / 0.6)
    phase = 2 * np.pi * np.cumsum(freq * vib) / RATE
    saw = np.zeros_like(t)
    for k in range(1, 14):
        saw += np.sin(k * phase) / k * (1 if k < 6 else 0.6)
    breath = bandpass(rng.standard_normal(len(t)), 300, 2500) * 0.08
    tone = lowpass(saw + breath, brightness)
    return tone * env_adsr(len(t), 0.25, 0.3, 0, 0.9, 0.8)


def gong(freq, seconds, tau=1.8):
    t = t_axis(seconds)
    partials = [(1.0, 1.0), (1.48, 0.6), (2.1, 0.45), (2.76, 0.3), (3.9, 0.18), (5.2, 0.1)]
    out = np.zeros_like(t)
    for ratio, amp in partials:
        wobble = 1 + 0.002 * np.sin(2 * np.pi * 0.7 * t * ratio)
        out += amp * np.sin(2 * np.pi * freq * ratio * wobble * t) * np.exp(-t / (tau / ratio ** 0.5))
    return out * np.minimum(1, t / 0.004)


def whoosh(seconds, seed, lo=200, hi=3000, rising=True):
    rng = np.random.default_rng(seed)
    n = int(RATE * seconds)
    noise = rng.standard_normal(n)
    out = np.zeros(n)
    steps = 24
    for i in range(steps):
        f = lo + (hi - lo) * ((i / steps) if rising else (1 - i / steps))
        seg = slice(i * n // steps, (i + 1) * n // steps)
        out[seg] = bandpass(noise, max(60, f * 0.6), min(RATE / 2 - 100, f * 1.6))[seg]
    return out * np.hanning(n)


def shard_absorb():
    out = np.zeros(int(RATE * 1.1))
    place(out, whoosh(0.5, 3, 300, 2600, rising=True), 0.0, 0.5)
    for i, n in enumerate(['E5', 'B5', 'E6']):
        place(out, bell(note(n), 0.8, tau=0.35, brightness=0.6), 0.32 + i * 0.07, 0.5 - i * 0.1)
    place(out, shimmer(0.7, 4, base=3200), 0.35, 0.6)
    return normalize(out, 0.8)


def brand_apply():
    rng = np.random.default_rng(7)
    out = np.zeros(int(RATE * 1.8))
    sizzle = highpass(rng.standard_normal(int(RATE * 0.9)), 2500) * exp_decay(int(RATE * 0.9), 0.35)
    crackle = (rng.random(int(RATE * 0.9)) > 0.993) * rng.uniform(-1, 1, int(RATE * 0.9))
    place(out, sizzle * 0.35 + crackle * 0.5, 0.0)
    place(out, gong(note('D3'), 1.6, tau=1.2), 0.05, 0.9)
    place(out, gong(note('A3'), 1.2, tau=0.8), 0.06, 0.35)
    return normalize(out, 0.85)


def remnant_horn():
    out = np.zeros(int(RATE * 3.6))
    place(out, brass(note('D2'), 2.6, 1), 0.0, 0.8)
    place(out, brass(note('A2'), 2.2, 2), 0.25, 0.45)
    place(out, brass(note('D3'), 1.4, 3, brightness=3200), 1.4, 0.3)
    rumble = lowpass(np.random.default_rng(5).standard_normal(len(out)), 120) * np.hanning(len(out)) * 0.6
    out += rumble
    return normalize(out, 0.85, fade_fraction=0.15)


def remnant_slain():
    out = np.zeros(int(RATE * 3.5))
    place(out, gong(note('A1'), 3.4, tau=2.4), 0.0, 1.0)
    place(out, gong(note('E2'), 3.0, tau=1.8), 0.02, 0.5)
    for i, n in enumerate(['A3', 'C4', 'E4', 'A4']):
        place(out, brass(note(n), 2.0, 10 + i, vibrato=3.5, brightness=1800), 0.3, 0.12)
    return normalize(out, 0.88, fade_fraction=0.2)


def rank_slain():
    out = np.zeros(int(RATE * 1.8))
    place(out, gong(note('D2'), 1.7, tau=1.1), 0.0, 1.0)
    place(out, bell(note('D5'), 1.2, tau=0.5, brightness=0.5), 0.05, 0.25)
    place(out, bell(note('A5'), 1.0, tau=0.4, brightness=0.5), 0.12, 0.18)
    return normalize(out, 0.85)


def hunters_moon():
    rng = np.random.default_rng(9)
    seconds = 6.0
    out = np.zeros(int(RATE * seconds))
    wind = bandpass(rng.standard_normal(len(out)), 180, 900)
    wind *= (0.5 + 0.5 * np.sin(2 * np.pi * 0.23 * t_axis(seconds))) * np.hanning(len(out)) * 0.25
    out += wind
    for i in range(3):
        toll = gong(note('C2'), 3.0, tau=2.0) + 0.4 * bell(note('C4'), 3.0, tau=1.2, brightness=0.3)
        place(out, toll, 0.2 + i * 1.6, 0.9 - i * 0.15)
    return normalize(out, 0.85, fade_fraction=0.2)


def blink():
    out = np.zeros(int(RATE * 0.7))
    place(out, whoosh(0.4, 21, 3000, 400, rising=False), 0.0, 0.7)
    t = t_axis(0.3)
    zap = np.sin(2 * np.pi * (1800 - 1200 * t / 0.3) * t) * exp_decay(len(t), 0.08)
    place(out, zap, 0.25, 0.4)
    return normalize(out, 0.75)


def ward_break():
    rng = np.random.default_rng(31)
    out = np.zeros(int(RATE * 1.0))
    crash = highpass(rng.standard_normal(int(RATE * 0.5)), 3000) * exp_decay(int(RATE * 0.5), 0.09)
    place(out, crash, 0.0, 0.8)
    for _ in range(14):
        place(out, bell(rng.uniform(2500, 6000), 0.4, tau=0.12, brightness=0.8) * rng.uniform(0.1, 0.25), rng.uniform(0.0, 0.35))
    place(out, gong(note('E3'), 0.8, tau=0.3), 0.0, 0.3)
    return normalize(out, 0.8)


def ward_restore():
    out = np.zeros(int(RATE * 1.0))
    for i, n in enumerate(['G4', 'C5', 'E5', 'G5']):
        place(out, bell(note(n), 0.6, tau=0.3, brightness=0.4), i * 0.06, 0.3)
    place(out, whoosh(0.6, 33, 400, 3500, rising=True), 0.0, 0.3)
    return normalize(out, 0.7)


def pack_call():
    """A long synthetic howl: a gliding voice with vowel formants and a second, lower voice."""
    seconds = 2.4
    t = t_axis(seconds)
    out = np.zeros_like(t)
    for base, gain, delay in ((note('A4'), 1.0, 0.0), (note('E4'), 0.5, 0.35)):
        f = base * (0.75 + 0.35 * np.sin(np.pi * np.clip((t - delay) / (seconds - delay), 0, 1)) ** 0.6)
        phase = 2 * np.pi * np.cumsum(f) / RATE
        voice = sum(np.sin(k * phase) / k ** 1.3 for k in range(1, 9))
        voice = bandpass(voice, 300, 2400)
        env = np.clip((t - delay) / 0.3, 0, 1) * np.clip((seconds - t) / 0.6, 0, 1)
        out += voice * env * gain
    return normalize(out, 0.8)


def volatile_fuse():
    rng = np.random.default_rng(41)
    seconds = 1.5
    n = int(RATE * seconds)
    t = t_axis(seconds)
    hiss = highpass(rng.standard_normal(n), 1800) * (0.3 + 0.7 * t / seconds)
    crackle = (rng.random(n) > 0.994) * rng.uniform(-1, 1, n)
    tone = np.sin(2 * np.pi * (220 + 660 * (t / seconds) ** 2) * t) * 0.15 * t / seconds
    return normalize(hiss * 0.5 + crackle * 0.6 + tone, 0.8, fade_fraction=0.02)


SOUNDS = {
    'shard_absorb': shard_absorb,
    'brand_apply': brand_apply,
    'remnant_horn': remnant_horn,
    'remnant_slain': remnant_slain,
    'rank_slain': rank_slain,
    'hunters_moon': hunters_moon,
    'blink': blink,
    'ward_break': ward_break,
    'ward_restore': ward_restore,
    'pack_call': pack_call,
    'volatile_fuse': volatile_fuse,
}

# sound event id -> (files, subtitle key)
EVENTS = {
    'item.shard.absorb': (['shard_absorb'], 'item.shard.absorb'),
    'item.brand.apply': (['brand_apply'], 'item.brand.apply'),
    'mob.remnant.horn': (['remnant_horn'], 'mob.remnant.horn'),
    'mob.remnant.slain': (['remnant_slain'], 'mob.remnant.slain'),
    'mob.rank_slain': (['rank_slain'], 'mob.rank_slain'),
    'event.hunters_moon': (['hunters_moon'], 'event.hunters_moon'),
    'trait.blink': (['blink'], 'trait.blink'),
    'trait.ward_break': (['ward_break'], 'trait.ward_break'),
    'trait.ward_restore': (['ward_restore'], 'trait.ward_restore'),
    'trait.pack_call': (['pack_call'], 'trait.pack_call'),
    'trait.volatile_fuse': (['volatile_fuse'], 'trait.volatile_fuse'),
}
