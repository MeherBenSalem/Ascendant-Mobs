package com.nightbeam.remnants.mobs.client;

/** ARGB helpers (1.21.1 has no ARGB utility class). */
public final class Colors {
    private Colors() {
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int color(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    public static int lerp(float t, int from, int to) {
        int a = Math.round(alpha(from) + (alpha(to) - alpha(from)) * t);
        int r = Math.round(((from >> 16) & 255) + (((to >> 16) & 255) - ((from >> 16) & 255)) * t);
        int g = Math.round(((from >> 8) & 255) + (((to >> 8) & 255) - ((from >> 8) & 255)) * t);
        int b = Math.round((from & 255) + ((to & 255) - (from & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
