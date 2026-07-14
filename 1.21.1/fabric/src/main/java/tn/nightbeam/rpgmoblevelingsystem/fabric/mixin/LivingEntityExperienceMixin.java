package tn.nightbeam.rpgmoblevelingsystem.fabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;

@Mixin(LivingEntity.class)
public abstract class LivingEntityExperienceMixin {
    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    private void rml$scaleExperience(CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        cir.setReturnValue(MobLevelService.adjustDroppedExperience(self, cir.getReturnValue()));
    }
}
