"""Sound synthesis helpers shared by sounds.py: envelopes, filters, bells and OGG encoding.

Requires numpy, scipy and ffmpeg with libvorbis. Deterministic: every random source is seeded.
"""
from __future__ import annotations

import subprocess
import tempfile
import wave
from pathlib import Path

import numpy as np
from scipy import signal

RATE = 44100


def t_axis(seconds):
    return np.arange(int(RATE * seconds)) / RATE


def env_adsr(n, attack, decay, sustain, release, sustain_level=0.6):
    a, d, r = int(attack * RATE), int(decay * RATE), int(release * RATE)
    s = max(0, n - a - d - r)
    return np.concatenate([np.linspace(0, 1, a, endpoint=False), np.linspace(1, sustain_level, d, endpoint=False),
                           np.full(s, sustain_level), np.linspace(sustain_level, 0, r)])[:n]


def exp_decay(n, tau):
    return np.exp(-np.arange(n) / (tau * RATE))


def bandpass(x, lo, hi, order=3):
    sos = signal.butter(order, [lo, hi], btype='band', fs=RATE, output='sos')
    return signal.sosfilt(sos, x)


def lowpass(x, cutoff, order=3):
    sos = signal.butter(order, cutoff, btype='low', fs=RATE, output='sos')
    return signal.sosfilt(sos, x)


def highpass(x, cutoff, order=3):
    sos = signal.butter(order, cutoff, btype='high', fs=RATE, output='sos')
    return signal.sosfilt(sos, x)


def bell(freq, seconds, tau=0.5, brightness=1.0):
    """A small metallic bell: inharmonic partials with faster decay for higher ones."""
    t = t_axis(seconds)
    partials = [(1.0, 1.0, 1.0), (2.0, 0.55, 0.7), (2.76, 0.35 * brightness, 0.45), (5.4, 0.18 * brightness, 0.25),
                (8.93, 0.08 * brightness, 0.15)]
    out = np.zeros_like(t)
    for ratio, amp, life in partials:
        out += amp * np.sin(2 * np.pi * freq * ratio * t) * np.exp(-t / (tau * life))
    attack = np.minimum(1.0, t / 0.003)
    return out * attack


def rustle(seconds, seed, bursts=3, lo=900, hi=6500):
    """Cloth: a few band-limited noise bursts with crackle."""
    rng = np.random.default_rng(seed)
    n = int(RATE * seconds)
    out = np.zeros(n)
    total = np.zeros(n)
    noise = bandpass(rng.standard_normal(n), lo, hi)
    for i in range(bursts):
        start = int(n * (i / bursts) * 0.8 + rng.integers(0, int(0.02 * RATE)))
        length = int(rng.uniform(0.05, 0.11) * RATE)
        e = np.zeros(n)
        seg = np.hanning(length * 2)[:length] ** 0.5 * exp_decay(length, rng.uniform(0.03, 0.06))
        end = min(n, start + length)
        e[start:end] = seg[:end - start]
        total = np.maximum(total, e)
        out += noise * e * rng.uniform(0.6, 1.0)
    crackle = (rng.random(n) > 0.9965).astype(float) * rng.uniform(-1, 1, n)
    out += highpass(crackle, 2500) * 0.25 * total
    return out


def thump(seconds=0.18, freq=95):
    t = t_axis(seconds)
    sweep = freq * (1 + 1.5 * np.exp(-t / 0.02))
    phase = 2 * np.pi * np.cumsum(sweep) / RATE
    return np.sin(phase) * exp_decay(len(t), 0.05)


def place(dest, src, at_seconds, gain=1.0):
    start = int(at_seconds * RATE)
    end = min(len(dest), start + len(src))
    if end > start:
        dest[start:end] += src[:end - start] * gain
    return dest


def normalize(x, peak=0.89, fade_fraction=0.0):
    x = x - np.mean(x)
    m = np.max(np.abs(x)) or 1.0
    fade = max(int(0.01 * RATE), int(len(x) * fade_fraction))
    x = x / m * peak
    x[-fade:] *= np.linspace(1, 0, fade)
    return x


def note(name):
    names = {'C': -9, 'C#': -8, 'D': -7, 'D#': -6, 'E': -5, 'F': -4, 'F#': -3, 'G': -2, 'G#': -1, 'A': 0, 'A#': 1, 'B': 2}
    pitch, octave = name[:-1], int(name[-1])
    return 440.0 * 2 ** ((names[pitch] + (octave - 4) * 12) / 12)


def shimmer(seconds, seed, base=3000):
    """A soft cloud of tiny high bells."""
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * seconds))
    for _ in range(int(seconds * 22)):
        f = base * rng.uniform(1.0, 2.2)
        b = bell(f, 0.35, tau=0.12, brightness=0.4) * rng.uniform(0.05, 0.16)
        place(out, b, rng.uniform(0, seconds * 0.8))
    return out



def write_ogg(samples, dest: Path):
    dest.parent.mkdir(parents=True, exist_ok=True)
    pcm = (np.clip(samples, -1, 1) * 32767).astype(np.int16)
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as tmp:
        with wave.open(tmp.name, 'wb') as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes(pcm.tobytes())
        subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', tmp.name, '-c:a', 'libvorbis', '-q:a', '5', str(dest)], check=True)


