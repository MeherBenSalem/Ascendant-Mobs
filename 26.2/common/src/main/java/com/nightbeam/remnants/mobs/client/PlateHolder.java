package com.nightbeam.remnants.mobs.client;

import org.jspecify.annotations.Nullable;

/** Implemented by entity render states through a mixin: the plate extracted for this frame. */
public interface PlateHolder {
    @Nullable Plate remnants$plate();

    void remnants$setPlate(@Nullable Plate plate);
}
