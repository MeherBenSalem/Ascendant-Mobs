package com.nightbeam.remnants.mobs.mob;

import net.minecraft.server.level.ServerLevel;

/** Overworld time, read the same way everywhere (the API moved between versions). */
public final class WorldTime {
    public static final int DAY = 24000;

    private WorldTime() {
    }

    /** Ticks on the overworld clock, including skipped nights. */
    public static long time(ServerLevel level) {
        return level.getDayTime();
    }

    public static long dayIndex(ServerLevel level) {
        return Math.floorDiv(time(level), DAY);
    }

    public static double day(ServerLevel level) {
        return Math.max(0, time(level)) / (double) DAY;
    }

    public static int timeOfDay(ServerLevel level) {
        return (int) Math.floorMod(time(level), DAY);
    }

    /** Vanilla moon phases: a full moon every eighth night. */
    public static boolean fullMoon(ServerLevel level) {
        return Math.floorMod(dayIndex(level), 8) == 0;
    }

    public static boolean night(ServerLevel level) {
        int t = timeOfDay(level);
        return t >= 13000 && t < 23000;
    }
}
