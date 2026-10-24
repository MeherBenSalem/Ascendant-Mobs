package com.nightbeam.remnants.mobs.mixin;

import com.nightbeam.remnants.mobs.mob.MobState;
import com.nightbeam.remnants.mobs.mob.RemnantMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin implements RemnantMob {
    @Unique
    private final MobState remnants$state = new MobState();

    @Override
    public MobState remnants$state() {
        return remnants$state;
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void remnants$rememberSpawner(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                          SpawnGroupData groupData, CompoundTag dataTag,
                                          CallbackInfoReturnable<SpawnGroupData> cir) {
        if (reason == MobSpawnType.SPAWNER) {
            remnants$state.fromSpawner = true;
        }
    }
}
