package com.nightbeam.remnants.mobs.mob;

import org.jspecify.annotations.Nullable;

/** Per-mob state Remnants keeps in memory, attached to every mob by a mixin. Only {@link #data} is saved (as a tag). */
public final class MobState {
    /** Level, rank and traits; null until the mob is leveled (server) or its info arrives (client). */
    public @Nullable MobData data;
    /** Server: the saved tag was read. */
    public boolean decoded;
    /** Server: the first tick ran (leveling happens there). */
    public boolean checked;
    /** Server: spawned by a monster spawner (set in finalizeSpawn). */
    public boolean fromSpawner;
    /** Server: game time of the last hit taken. */
    public long lastHurt = -1000;
    /** Server: game time Blinking is ready again. */
    public long blinkReady;
    /** Server: Berserk modifiers are on. */
    public boolean berserk;
    /** Server: the absorption shield of Warded is up. */
    public boolean wardUp;

    /** Client: synced attack damage and armor, for the Hunter's Lens. */
    public float damage;
    public float armor;
    /** Client: health shown on the plate a moment ago, for the trailing damage segment. */
    public float trailHealth = -1;
    public long trailTime;
    public float lastSeenHealth = -1;
    public long trailFrame;
}
