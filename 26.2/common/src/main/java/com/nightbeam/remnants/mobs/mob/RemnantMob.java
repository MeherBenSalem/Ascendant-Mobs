package com.nightbeam.remnants.mobs.mob;

import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/** Implemented by every {@link Mob} through a mixin. */
public interface RemnantMob {
    MobState remnants$state();

    static MobState state(Mob mob) {
        return ((RemnantMob) mob).remnants$state();
    }

    /** The mob's data, reading its saved tag the first time. Works on both sides. */
    static @Nullable MobData data(Mob mob) {
        MobState state = state(mob);
        if (!state.decoded && !mob.level().isClientSide()) {
            state.decoded = true;
            for (String tag : mob.entityTags()) {
                MobData decoded = MobData.decode(tag);
                if (decoded != null) {
                    state.data = decoded;
                    break;
                }
            }
        }
        return state.data;
    }
}
