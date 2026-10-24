package com.nightbeam.remnants.mobs.mixin.client;

import com.nightbeam.remnants.mobs.client.Plate;
import com.nightbeam.remnants.mobs.client.PlateHolder;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements PlateHolder {
    @Unique
    private @Nullable Plate remnants$plate;

    @Override
    public @Nullable Plate remnants$plate() {
        return remnants$plate;
    }

    @Override
    public void remnants$setPlate(@Nullable Plate plate) {
        remnants$plate = plate;
    }
}
