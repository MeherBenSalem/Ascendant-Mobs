package tn.nightbeam.rpgmoblevelingsystem.util;

/**
 * Pure attribution priority for non-melee outgoing damage.
 * Mirrors {@code OutgoingDamageScaling.resolveAttackerFromEntities} without needing Minecraft entities.
 */
public final class DamageAttributionPaths {
    public static final int NONE = 0;
    public static final int CAUSING_LIVING = 1;
    public static final int CAUSING_PROJECTILE_OWNER = 2;
    public static final int DIRECT_PROJECTILE_OWNER = 3;
    public static final int DIRECT_LIVING = 4;

    private DamageAttributionPaths() {
    }

    /**
     * @param causingLiving            {@code getEntity()} is a living attacker
     * @param causingProjectileOwner   {@code getEntity()} is a projectile with a living owner
     * @param directProjectileOwner    {@code getDirectEntity()} is a projectile with a living owner
     * @param directLiving             {@code getDirectEntity()} is living
     */
    public static int choose(
            boolean causingLiving,
            boolean causingProjectileOwner,
            boolean directProjectileOwner,
            boolean directLiving) {
        if (causingLiving) {
            return CAUSING_LIVING;
        }
        if (causingProjectileOwner) {
            return CAUSING_PROJECTILE_OWNER;
        }
        if (directProjectileOwner) {
            return DIRECT_PROJECTILE_OWNER;
        }
        if (directLiving) {
            return DIRECT_LIVING;
        }
        return NONE;
    }
}
