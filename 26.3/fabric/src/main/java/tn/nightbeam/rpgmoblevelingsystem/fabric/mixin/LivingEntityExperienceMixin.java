package tn.nightbeam.rpgmoblevelingsystem.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.OutgoingDamageScaling;

@Mixin(LivingEntity.class)
public abstract class LivingEntityExperienceMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float rml$scaleOutgoingDamage(float amount, ServerLevel level, DamageSource source) {
        return OutgoingDamageScaling.scale(source, amount);
    }

    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    private void rml$scaleExperience(ServerLevel level, Entity attacker, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        cir.setReturnValue(MobLevelService.adjustDroppedExperience(self, cir.getReturnValue()));
    }
}
