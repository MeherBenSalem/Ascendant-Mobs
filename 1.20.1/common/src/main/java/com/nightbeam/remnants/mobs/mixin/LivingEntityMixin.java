package com.nightbeam.remnants.mobs.mixin;

import com.nightbeam.remnants.mobs.content.Trait;
import com.nightbeam.remnants.mobs.mob.MobData;
import com.nightbeam.remnants.mobs.mob.MobLeveler;
import com.nightbeam.remnants.mobs.mob.MobState;
import com.nightbeam.remnants.mobs.mob.RemnantMob;
import com.nightbeam.remnants.mobs.reward.Rewards;
import com.nightbeam.remnants.mobs.trait.TraitEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private float remnants$before;
    @Unique
    private boolean remnants$hadShield;

    @Shadow
    protected int lastHurtByPlayerTime;

    @Shadow
    protected abstract boolean shouldDropLoot();

    @Shadow
    protected abstract void dropFromLootTable(DamageSource source, boolean playerKilled);

    @Inject(method = "tick", at = @At("HEAD"))
    private void remnants$tick(CallbackInfo ci) {
        if ((Object) this instanceof Mob mob && mob.level() instanceof ServerLevel level) {
            MobState state = RemnantMob.state(mob);
            if (!state.checked) {
                MobLeveler.firstTick(level, mob);
            }
            MobData data = state.data;
            if (data != null) {
                TraitEffects.tick(level, mob, data);
            }
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float remnants$beforeHurt(float damage, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) {
            return damage;
        }
        remnants$before = self.getHealth() + self.getAbsorptionAmount();
        remnants$hadShield = self.getAbsorptionAmount() > 0;
        return TraitEffects.beforeHurt(level, self, source, damage);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void remnants$afterHurt(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (cir.getReturnValueZ() && self.level() instanceof ServerLevel level) {
            float dealt = Math.max(0.0F, remnants$before - (self.getHealth() + self.getAbsorptionAmount()));
            TraitEffects.afterHurt(level, self, source, dealt, remnants$hadShield);
        }
    }

    // Mobs override getExperienceReward() in 1.20.1, so the amount is changed where the orbs are created.
    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"),
            index = 2)
    private int remnants$experience(int amount) {
        return (Object) this instanceof Mob mob ? Rewards.experience(mob, amount) : amount;
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void remnants$extraLoot(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof Mob mob && mob.level() instanceof ServerLevel level && shouldDropLoot()
                && level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
            boolean playerKilled = lastHurtByPlayerTime > 0;
            Rewards.dropLoot(level, mob, source, playerKilled, () -> dropFromLootTable(source, playerKilled));
        }
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    private void remnants$frostbite(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && (Object) this instanceof Mob mob) {
            MobData data = RemnantMob.state(mob).data;
            if (data != null && data.has(Trait.FROSTBITE)) {
                cir.setReturnValue(false);
            }
        }
    }
}
