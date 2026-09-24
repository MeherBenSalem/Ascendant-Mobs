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
        if (attacker == null || attacker.level().isClientSide()) {
            return amount;
        }
        if (MobLevelStorage.getStoredLevel(attacker) == null) {
            return amount;
        }
        return AttributeScalingService.scaleOutgoingNonMeleeDamage(attacker, amount);
    }

    private static LivingEntity resolveAttacker(DamageSource source) {
        return resolveAttackerFromEntities(source.getEntity(), source.getDirectEntity());
    }

    /**
     * Attribution order matches {@link tn.nightbeam.rpgmoblevelingsystem.util.DamageAttributionPaths#choose}.
     * Package-visible for tests of projectile-as-causing-entity handling.
     */
    static LivingEntity resolveAttackerFromEntities(Entity causing, Entity direct) {
        if (causing instanceof LivingEntity living) {
            return living;
        }
        LivingEntity fromCausingProjectile = ownerIfProjectile(causing);
        if (fromCausingProjectile != null) {
            return fromCausingProjectile;
        }
        LivingEntity fromDirectProjectile = ownerIfProjectile(direct);
        if (fromDirectProjectile != null) {
            return fromDirectProjectile;
        }
        if (direct instanceof LivingEntity livingDirect) {
            return livingDirect;
        }
        return null;
    }

    private static LivingEntity ownerIfProjectile(Entity entity) {
        if (entity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) {
            return owner;
        }
        return null;
    }

    private static boolean usesMeleeAttackDamageAttribute(DamageSource source) {
        return source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                || source.is(DamageTypes.PLAYER_ATTACK);
    }
}
