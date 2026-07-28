package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Scales projectile / sonic / other non-melee damage from leveled mobs.
 * Melee already uses {@code ATTACK_DAMAGE} attribute modifiers and must not be scaled twice.
 */
public final class OutgoingDamageScaling {
    private OutgoingDamageScaling() {
    }

    public static float scale(DamageSource source, float amount) {
        if (amount <= 0.0f || usesMeleeAttackDamageAttribute(source)) {
            return amount;
        }
        LivingEntity attacker = resolveAttacker(source);
        if (attacker == null || attacker.level().isClientSide) {
            return amount;
        }
        if (MobLevelStorage.getStoredLevel(attacker) == null) {
            return amount;
        }
        return AttributeScalingService.scaleOutgoingNonMeleeDamage(attacker, amount);
    }

    private static LivingEntity resolveAttacker(DamageSource source) {
        Entity causing = source.getEntity();
        if (causing instanceof LivingEntity living) {
            return living;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) {
            return owner;
        }
        if (direct instanceof LivingEntity livingDirect) {
            return livingDirect;
        }
        return null;
    }

    private static boolean usesMeleeAttackDamageAttribute(DamageSource source) {
        return source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                || source.is(DamageTypes.PLAYER_ATTACK);
    }
}
