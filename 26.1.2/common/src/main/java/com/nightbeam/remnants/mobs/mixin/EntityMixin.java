package com.nightbeam.remnants.mobs.mixin;

import com.nightbeam.remnants.mobs.content.Trait;
import com.nightbeam.remnants.mobs.mob.MobData;
import com.nightbeam.remnants.mobs.mob.RemnantMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
    private void remnants$infernal(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && (Object) this instanceof Mob mob) {
            MobData data = RemnantMob.state(mob).data;
            if (data != null && data.has(Trait.INFERNAL)) {
                cir.setReturnValue(true);
            }
        }
    }
}
